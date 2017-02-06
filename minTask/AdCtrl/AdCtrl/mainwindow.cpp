#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QMessageBox>

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    port_open_flag = false;
    uartStringList = enumSeriPort();
    if(!uartStringList.isEmpty())
    {
        ui->comboBox_port->addItems(uartStringList);
    }

    totalRecved = msgLenth = point = startFrame = startPoint = 0;
    connect(&serial, SIGNAL(readyRead()), this, SLOT(readData()));
}


MainWindow::~MainWindow()
{
    delete ui;
}

QStringList MainWindow::enumSeriPort()
{
    QStringList PortList;
    PortList.clear();

    foreach (const QSerialPortInfo &info, QSerialPortInfo::availablePorts())
    {
            PortList.append(info.portName());
    }

    return PortList;
}


int MainWindow::open_port(QString port)
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
            ui->statusBar->showMessage(tr("Connected to %1 : %2, %3, %4, %5, %6")
                                       .arg(serialPortNum).arg("115200").arg("Data8")
                                       .arg("NoParity").arg("OneStop").arg("NoFlowControl"));
            return 0;
    } else {
        QMessageBox::critical(this, tr("Error"), serial.errorString());

        ui->statusBar->showMessage(tr("Open error"));
        return -1;
    }
}

void MainWindow::close_port()
{
    if(serial.isOpen())
    {
        serial.close();
        ui->statusBar->showMessage("Serial Port closed!");
    }
}

void MainWindow::on_pushButton_serialPort_clicked()
{
    QString serialPortNum;
    serialPortNum = ui->comboBox_port->currentText();
    if(serialPortNum.isEmpty())
        return;

    if(port_open_flag == false)
    {
        int resust = open_port(serialPortNum);
        if(resust == 0)
            port_open_flag = true;
        else
            port_open_flag = false;
        ui->pushButton_serialPort->setText("close_port");
    }
    else
    {
           this->close_port();
           port_open_flag = false;
           ui->pushButton_serialPort->setText("open_port");
    }
}


void MainWindow::on_pushButton_getCurrentFile_clicked()
{
    char send_data[128] = {0};
    char cherk_or = 0;
    unsigned short msg_length = 0;
    msg_length = 8;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = GET_CURRENT_FILE;
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    send_data[5] = 0xff; //哑数据2字节0xff
    send_data[6] = 0xff;
    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[7] = cherk_or;
    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
    }
}

void MainWindow::on_pushButton_getVideoList_clicked()
{
    char send_data[128] = {0};
    char cherk_or = 0;
    unsigned short msg_length = 0;
    msg_length = 8;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = GET_FILE_lIST;   //ID
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    send_data[5] = 0xff; //哑数据2字节0xff
    send_data[6] = 0xff;
    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[7] = cherk_or;
    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
    }

    ui->listWidget->clear();
}



void MainWindow::on_pushButton_play_after_clicked()
{
    char send_data[128] = {0};
    char cherk_or = 0;
    unsigned short msg_length = 0;
    msg_length = 8;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = 0x06;   //ID
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    send_data[5] = 0xff; //哑数据2字节0xff
    send_data[6] = 0xff;
    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[7] = cherk_or;
    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
        serial.flush();
    }
}

void MainWindow::on_pushButton_play_before_clicked()
{
    char send_data[128] = {0};
    char cherk_or = 0;
    unsigned short msg_length = 0;
    msg_length = 8;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = 0x07;    //ID
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    send_data[5] = 0xff; //哑数据2字节0xff
    send_data[6] = 0xff;
    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[7] = cherk_or;
    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
    }
}

void MainWindow::on_pushButton_pause_clicked()
{
    char send_data[128] = {0};
    char cherk_or = 0;
    unsigned short msg_length = 0;
    msg_length = 8;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = 0x04;    //ID
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    send_data[5] = 0xff; //哑数据2字节0xff
    send_data[6] = 0xff;
    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[7] = cherk_or;
    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
    }
}

void MainWindow::on_pushButto_play_clicked()
{
    char send_data[128] = {0};
    char cherk_or = 0;
    unsigned short msg_length = 0;
    msg_length = 8;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = 0x03;    //ID
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    send_data[5] = 0xff; //哑数据2字节0xff
    send_data[6] = 0xff;
    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[7] = cherk_or;
    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
    }
}

void MainWindow::on_pushButton_close_clicked()
{
    char send_data[128] = {0};
    char cherk_or = 0;
    unsigned short msg_length = 0;
    msg_length = 8;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = 0x05;    //ID
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    send_data[5] = 0xff; //哑数据2字节0xff
    send_data[6] = 0xff;
    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[7] = cherk_or;
    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
    }
}


void MainWindow::on_pushButton_play_target_vedio_clicked()
{
    char send_data[128] = {0};

    char cherk_or = 0;
    unsigned short file_length = 0;
    unsigned short msg_length = 0;
    QString target_file = ui->lineEdit_targetFile->text();
    if(target_file.isEmpty())
    {
        QMessageBox::critical(this, "file is not valid", "please input legal file name!");
        return;
    }
    file_length = target_file.toLocal8Bit().length();  //求说占字节数
    msg_length = file_length + 6;
    send_data[0] = 0x8f;
    send_data[1] = 0x8f;
    send_data[2] = PLAY_TARGET_FILE;    //ID
    send_data[3] = (char)msg_length;
    send_data[4] = 0x00;
    memcpy(&send_data[5], target_file.toStdString().c_str(), file_length);

    for(int i =0 ; i < (msg_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^send_data[i];
    }
    send_data[msg_length - 1] = cherk_or;

    if(serial.isOpen())
    {
        serial.write(send_data, msg_length);
    }
}


void MainWindow::readData()
{
    long long recvLen;
    recvLen = serial.read(recvBufer+point, 1024);
    if(recvLen <= 0)
        return;
    point = point + recvLen;

    //if(point > ??)  //如果过大,表示报文解析由误了需要 置0
    if(msgLenth > 0)
    {
        if(point < msgLenth)                   //长度不够,下次继续读
            return;
        else                                   //recvBufer有了至少完整的一帧数据了!
        {
            memcpy(fullMsgBuf, &recvBufer[startFrame], msgLenth);   //取出完整的一帧数据
            handlCompleteData();                                    //处理数据
            if(point == msgLenth)                                   //刚好完整一帧数据
            {
               point = msgLenth = startFrame = startPoint = 0;           //下次进来进入找头分支
               return;
            }
            startPoint = startFrame + msgLenth; //?
            msgLenth = startFrame = 0;              //马上进入找头分支
        }
    }

    for(quint16 i = startPoint; i < point; i++)
    {
        if(*(unsigned short *)&recvBufer[i] == 0x8f8f)
        {
            startFrame = i;        //记录启始帧的位置
            if(point - startFrame >= 5)//有完整的头
            {
                msgLenth = *(unsigned short *)&recvBufer[startFrame + 3];  //取出长度
                if(msgLenth > 2048)  //如果过大,表示帧由问题
                {
                    qDebug()<<"数据帧长度大于2k,数据丢弃";
                    startFrame = msgLenth = point = startPoint= 0;
                }
                if((startFrame + msgLenth) <=point )  //当前数据有完整的帧
                {
                    memcpy(fullMsgBuf, &recvBufer[startFrame], msgLenth);
                    handlCompleteData();
                    if((startFrame + msgLenth) == point)        //刚好一帧结束
                    {
                        startFrame = msgLenth = point = startPoint= 0;
                        return;
                    }
                    else  //有多的,继续处理
                    {
                        startPoint = i = (startFrame + msgLenth) - 1;             //必须加-1,因为continue后i会+1
                        //start = startFrame + msgLenth;                       //remade 2014-11-24
                        continue;
                    }
                }
                else  //有完整的帧头,但是数据长度不够的情况下,把剩下的全部复制到recvBufer的最前头
                {
                    int cpLenth = point-startFrame;
                    memcpy(recvBufer, &recvBufer[startFrame], cpLenth);
                    startFrame = startPoint = 0;
                    point = cpLenth;
                    return;
                }
            }
            else  //找到了信息头,但是报文头不够长的情况下(找到了8个8f但剩下的数据不够25字节头),把数据复制到recvBufer的最前面
            {
                int cpLenth = point - startFrame;
                memcpy(recvBufer, &recvBufer[startFrame], cpLenth);
                startFrame = 0;
                point = cpLenth;
                msgLenth = startPoint = 0;  //下次继续进来找头
                return;
            }
         }
    }
}


void MainWindow::handlCompleteData()
{
    char cherk_or = 0;
    char msgId = 0;
    quint16 rd_length;
    quint16 wr_length;
    quint16 file_length;
    char wr_data[128] = {0};

    msgId = fullMsgBuf[2];
    rd_length = *(unsigned short *)&fullMsgBuf[3];


    for(int i =0 ; i < (rd_length - 1) ;i ++)
    {
        cherk_or =  cherk_or^fullMsgBuf[i];
    }

    if(cherk_or != fullMsgBuf[rd_length - 1])
    {
        qDebug() << "cherk_or error";
        return;
    }

    msgId = fullMsgBuf[2];
    if(msgId == ERROR_NOFILE)      //停止播放
    {
        QMessageBox::warning(this, "错误", "没有该视频文件");
    }
    else if(msgId == PUT_CURRENT_FILE)
    {
        file_length = rd_length - 6;
        fullMsgBuf[rd_length - 1] = '\0';
        QString target_file = QString::fromLocal8Bit(&fullMsgBuf[5]);
        ui->lineEdit_current_video->setText(target_file);
    }
    else if(msgId == PUT_FILE_LIST)
    {
        file_length = rd_length - 6;
        fullMsgBuf[rd_length - 1] = '\0';
        QString target_file = QString::fromLocal8Bit(&fullMsgBuf[5]);

        ui->listWidget->addItem(new QListWidgetItem(target_file));
        //ui->lineEdit_current_video->setText(target_file);
    }

}
