/******************************
 * Qt player using libVLC     *
 * By protonux                *
 *                            *
 * Under WTFPL                *
 ******************************/

#include "player.h"
#include <vlc/vlc.h>
#include <unistd.h>

#define qtu( i ) ((i).toUtf8().constData())

#include <QtGui>

Mwindow::Mwindow() {
    vlcPlayer = NULL;                       //libvlc_media_player_t *

    /* Initialize libVLC */
    vlcInstance = libvlc_new(0, NULL);      //libvlc_instance_t *

    /* Complain in case of broken installation */
    if (vlcInstance == NULL) {              //检验VLC是否正确安装
        QMessageBox::critical(this, "Qt libVLC player", "Could not init libVLC");
        exit(1);
    }

    connect(&serial, SIGNAL(readyRead()), this, SLOT(readSerialData()));

    this->open_port("/dev/ttyS1");
    /* Interface initialization */
    initUI();                           //


    playList();

}

Mwindow::~Mwindow() {
    /* Release libVLC instance on quit */
    if (vlcInstance)
        libvlc_release(vlcInstance);
}

void Mwindow::initUI() {


    /* Menu */
    QMenu *fileMenu = menuBar()->addMenu("&File");
    QMenu *editMenu = menuBar()->addMenu("&Edit");

    QAction *Open    = new QAction("&Open", this);
    QAction *Quit    = new QAction("&Quit", this);
    QAction *playAc  = new QAction("&Play/Pause", this);
    QAction *fsAc  = new QAction("&Fullscreen", this);
    QAction *aboutAc = new QAction("&About", this);

    Open->setShortcut(QKeySequence("Ctrl+O"));    //设置快捷键
    Quit->setShortcut(QKeySequence("Ctrl+Q"));

    fileMenu->addAction(Open);
    fileMenu->addAction(aboutAc);
    fileMenu->addAction(Quit);
    editMenu->addAction(playAc);
    editMenu->addAction(fsAc);

    connect(Open,    SIGNAL(triggered()), this, SLOT(openFile()));
    connect(playAc,  SIGNAL(triggered()), this, SLOT(play()));
    connect(aboutAc, SIGNAL(triggered()), this, SLOT(about()));
    connect(fsAc,    SIGNAL(triggered()), this, SLOT(fullscreen()));
    connect(Quit,    SIGNAL(triggered()), qApp, SLOT(quit()));

    /* Buttons for the UI */
    playBut = new QPushButton("Play");
    QObject::connect(playBut, SIGNAL(clicked()), this, SLOT(play()));

    stopBut = new QPushButton("Stop");
    QObject::connect(stopBut, SIGNAL(clicked()), this, SLOT(stop()));

    muteBut = new QPushButton("Mute");
    QObject::connect(muteBut, SIGNAL(clicked()), this, SLOT(mute()));

    fsBut = new QPushButton("Fullscreen");
    QObject::connect(fsBut, SIGNAL(clicked()), this, SLOT(fullscreen()));

    /* 声音滑动条 */
    volumeSlider = new QSlider(Qt::Horizontal);
    QObject::connect(volumeSlider, SIGNAL(sliderMoved(int)), this, SLOT(changeVolume(int)));
    volumeSlider->setValue(80);

    /* 进度滑动条 */
    slider = new QSlider(Qt::Horizontal);
    slider->setMaximum(1000);
    QObject::connect(slider, SIGNAL(sliderMoved(int)), this, SLOT(changePosition(int)));

    /* A timer to update the sliders */
    QTimer *timer = new QTimer(this);
    connect(timer, SIGNAL(timeout()), this, SLOT(updateInterface()));  //每隔30ms检查是否播放完毕
    timer->start(30);

    /* Central Widgets */
    QWidget* centralWidget = new QWidget;
    videoWidget = new QWidget;

    videoWidget->setAutoFillBackground( true );
    QPalette plt = palette();
    plt.setColor( QPalette::Window, Qt::black );
    videoWidget->setPalette( plt );

    /* Put all in layouts */
    QHBoxLayout *layout = new QHBoxLayout;
    layout->setMargin(0);
    layout->addWidget(playBut);
    layout->addWidget(stopBut);
    layout->addWidget(muteBut);
    layout->addWidget(fsBut);
    layout->addWidget(volumeSlider);


    QVBoxLayout *layout2 = new QVBoxLayout;
    layout2->setMargin(0);
    layout2->addWidget(videoWidget);
    layout2->addWidget(slider);
    layout2->addLayout(layout);


    centralWidget->setLayout(layout2);
    setCentralWidget(centralWidget);

    resize( 600, 400);
    sleep(1);
    this->fullscreen();
}

void Mwindow::openFile() {

    /* The basic file-select box */
    QString fileOpen = QFileDialog::getOpenFileName(this, tr("Load a file"), "~");

    /* Stop if something is playing */
    if (vlcPlayer && libvlc_media_player_is_playing(vlcPlayer))
        stop();

    /* Create a new Media */
    libvlc_media_t *vlcMedia = libvlc_media_new_path(vlcInstance, qtu(fileOpen));
    if (!vlcMedia)
        return;

    /* Create a new libvlc player */
    vlcPlayer = libvlc_media_player_new_from_media (vlcMedia);

    /* Release the media */
    libvlc_media_release(vlcMedia);

    /* Integrate the video in the interface */
#if defined(Q_OS_MAC)
    libvlc_media_player_set_nsobject(vlcPlayer, (void *)videoWidget->winId());
#elif defined(Q_OS_UNIX)
    libvlc_media_player_set_xwindow(vlcPlayer, videoWidget->winId());
#elif defined(Q_OS_WIN)
    libvlc_media_player_set_hwnd(vlcPlayer, videoWidget->winId());
#endif

    /* And start playback */
    libvlc_media_player_play (vlcPlayer);

    /* Update playback button */
    playBut->setText("Pause");
}

/* 打开视频槽函数 */
void Mwindow::play() {
    if (!vlcPlayer)
        return;

    if (libvlc_media_player_is_playing(vlcPlayer))
    {
        /* Pause */
        libvlc_media_player_pause(vlcPlayer);
        playBut->setText("Play");
    }
    else
    {
        /* Play again */
        libvlc_media_player_play(vlcPlayer);
        playBut->setText("Pause");
    }
}

int Mwindow::changeVolume(int vol) { /* Called on volume slider change */

    if (vlcPlayer)
        return libvlc_audio_set_volume (vlcPlayer,vol);  //0:静音 100:最大音

    return 0;
}

void Mwindow::changePosition(int pos) { /* Called on position slider change */

    if (vlcPlayer)
        libvlc_media_player_set_position(vlcPlayer, (float)pos/1000.0);
}

void Mwindow::updateInterface() { //Update interface and check if song is finished

    if (!vlcPlayer)
        return;

    /* update the timeline */
    float pos = libvlc_media_player_get_position(vlcPlayer);
    slider->setValue((int)(pos*1000.0));

    /* Stop the media */
    if (libvlc_media_player_get_state(vlcPlayer) == libvlc_Ended)  //end of file
    {
        this->stop();
        if(TotalVedio > 0)
        {
            if(ListId >= (TotalVedio-1) )
            {
                ListId = 0;
            }else
                ListId = ListId + 1;
            this->playNum(ListId);
            current_file = VedioToPlayList.at(ListId);
           // this->fullscreen();
        }


    }

}

void Mwindow::stop() {
    if(vlcPlayer) {
        /* stop the media player */
        libvlc_media_player_stop(vlcPlayer);

        /* release the media player */
        libvlc_media_player_release(vlcPlayer);

        /* Reset application values */
        vlcPlayer = NULL;
        slider->setValue(0);
        playBut->setText("Play");
    }
}

void Mwindow::playList()
{

    QStringList vedioList;
    QString homeDirStr = QDir::homePath();
    QString VideoDirStr = homeDirStr.append("/Videos");
    QDir videoDir = QDir(VideoDirStr);
    QStringList filters;
    filters << "*.rmvb" << "*.mp4" << "*.mkv" << "*.mov" << "*.wmv";
    videoDir.setNameFilters(filters);
    vedioList = videoDir.entryList();
    if(vedioList.isEmpty())
        return;

    TotalVedio = vedioList.size();
    for(int i = 0; i< TotalVedio; i++)
    {
        homeDirStr = QDir::homePath();
        VideoDirStr = homeDirStr.append("/Videos");
        QString fileToList = VideoDirStr.append("/");
        fileToList.append(vedioList.at(i));
        VedioToPlayList.append(fileToList);
    }

    ListId = 0;
    playNum(ListId);
    current_file = VedioToPlayList.at(ListId);
//    sleep(1);
//    this->fullscreen();


}

void Mwindow::playNum(int num)
{
    QString toFile = VedioToPlayList.at(num);
    /* Stop if something is playing */
    if (vlcPlayer && libvlc_media_player_is_playing(vlcPlayer))
        stop();

    /* Create a new Media */
    libvlc_media_t *vlcMedia = libvlc_media_new_path(vlcInstance, toFile.toStdString().c_str());
    if (!vlcMedia)
        return;

    /* Create a new libvlc player */
    vlcPlayer = libvlc_media_player_new_from_media (vlcMedia);

    /* Release the media */
    libvlc_media_release(vlcMedia);

    /* Integrate the video in the interface */
#if defined(Q_OS_MAC)
    libvlc_media_player_set_nsobject(vlcPlayer, (void *)videoWidget->winId());
#elif defined(Q_OS_UNIX)
    libvlc_media_player_set_xwindow(vlcPlayer, videoWidget->winId());
#elif defined(Q_OS_WIN)
    libvlc_media_player_set_hwnd(vlcPlayer, videoWidget->winId());
#endif

    /* And start playback */
    libvlc_media_player_play (vlcPlayer);


    /* Update playback button */
    playBut->setText("Pause");
}


void Mwindow::mute() {
    if(vlcPlayer) {
        if(volumeSlider->value() == 0) { //if already muted...

                this->changeVolume(80);
                volumeSlider->setValue(80);

        } else { //else mute volume

                this->changeVolume(0);
                volumeSlider->setValue(0);

        }
    }
}

void Mwindow::about()
{
    QMessageBox::about(this, "Qt libVLC player demo", QString::fromUtf8(libvlc_get_version()) );
}

void Mwindow::mouseDoubleClickEvent(QMouseEvent * event) //catch the doubleClick
{
    if(event->button()==Qt::LeftButton)
    {
        this->fullscreen();
    }

}

void Mwindow::fullscreen()
{
   if (isFullScreen()) {
       showNormal();
       menuWidget()->show();
       playBut->show();
       volumeSlider->show();
       slider->show();
       stopBut->show();
       muteBut->show();
       fsBut->show();
   }
   else {
       showFullScreen();
       menuWidget()->hide();
       playBut->hide();
       volumeSlider->hide();
       slider->hide();
       stopBut->hide();
       muteBut->hide();
       fsBut->hide();
   }
}

void Mwindow::closeEvent(QCloseEvent *event) {
    stop();
    event->accept();
}

int Mwindow::open_port(QString port)
{
    QString serialPortNum;
    serialPortNum = port;
    serial.setPortName(serialPortNum);
    serial.setBaudRate(QSerialPort::Baud115200);
    serial.setDataBits(QSerialPort::Data8);
    serial.setParity(QSerialPort::NoParity);
    serial.setStopBits(QSerialPort::OneStop);
    serial.setFlowControl(QSerialPort::NoFlowControl);
    if (serial.open(QIODevice::ReadWrite)) {
        qDebug()<<serialPortNum << "open sucess" ;
            return 0;
    } else {
            qDebug()<<serialPortNum << "open error" ;
        return -1;
    }
}

void Mwindow::close_port()
{
    if(serial.isOpen())
    {
        serial.close();
    }
}

void Mwindow::readSerialData()
{
    char rd_data[128] = {0};
    char wr_data[128] = {0};
    char cherk_or = 0;
    char msgId = 0;
    unsigned short rd_length;
    unsigned short wr_length;
    unsigned short file_length;
    serial.read(rd_data, 128);
    if((rd_data[0] != 0x8f) || (rd_data[1] != 0x8f))  //cherk head
    {
        return;
    }

    //0 1 2
    rd_length = rd_data[3] + rd_data[4] * 256;

    for(int i =0 ; i < (rd_length - 2) ;i ++)
    {
        cherk_or =  cherk_or^rd_data[i];
    }

    if(cherk_or != rd_data[rd_length - 1])
    {
        return;
    }

    msgId = rd_data[2];

    if(msgId == 0x01)           // 获取视频目录下的所有视频文件
    {
        for(int i = 0; i < VedioToPlayList.size(); i++)
        {
            file_length = VedioToPlayList.at(i).toLocal8Bit().length();
            wr_length = file_length + 6;
            wr_data[0] = 0x8f;
            wr_data[1] = 0x8f;
            wr_data[2] = 0x01;
            wr_data[3] =  wr_length;
            wr_data[4] = 0;

            memcpy(&send_data[5], VedioToPlayList.at(i).toStdString().c_str(), file_length);

            for(int i =0 ; i < wr_length - 1 ;i ++)
            {
                cherk_or =  cherk_or^wr_data[i];
            }

            wr_data[wr_length - 1] = cherk_or;   //填写校验位
            serial.write(wr_data, wr_length);
        }


    }
    else if(msgId == 0x02)      //获取当前播放的文件
    {
        file_length = current_file.toLocal8Bit().length();
        wr_length = file_length + 6;
        wr_data[0] = 0x8f;
        wr_data[1] = 0x8f;
        wr_data[2] = 0x01;
        wr_data[3] =  wr_length;
        wr_data[4] = 0;

        memcpy(&send_data[5], current_file.toStdString().c_str(), file_length);

        for(int i =0 ; i < wr_length - 1 ;i ++)
        {
            cherk_or =  cherk_or^wr_data[i];
        }

        wr_data[wr_length - 1] = cherk_or;   //填写校验位
        serial.write(wr_data, wr_length);
    }
    else if(msgId == 0x03)      //开始播放
    {

    }
    else if(msgId == 0x04)      //暂停播放
    {

    }
    else if(msgId == 0x05)      //停止播放
    {

    }
    else if(msgId == 0x06)      //播放下一文件
    {

    }
    else if(msgId == 0x07)      //播放上一文件
    {

    }
    else if(msgId == 0x08)      //播放指定的文件
    {

    }


}

