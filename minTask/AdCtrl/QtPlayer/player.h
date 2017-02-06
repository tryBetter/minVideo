/******************************
 * Qt player using libVLC     *
 * By protonux                *
 *                            *
 * Under WTFPL                *
 ******************************/

#ifndef PLAYER
#define PLAYER

#include <QtGui>
#include <QPushButton>
#include <QSlider>

#include <vlc/vlc.h>
#include <QGLWidget>

#include <QtSerialPort/QSerialPort>
#include <QtSerialPort/QSerialPortInfo>

class Mwindow : public QMainWindow {

    Q_OBJECT

        public:
               Mwindow();
               virtual ~Mwindow();
               int open_port(QString port);
               void close_port();
        private slots:
               void openFile();
               void play();
               void stop();
               void mute();
               void about();
               void fullscreen();

               int changeVolume(int);
               void changePosition(int);
               void updateInterface();

               void readSerialData();
        protected:
               virtual void closeEvent(QCloseEvent*);
               void mouseDoubleClickEvent(QMouseEvent * event);  //响应双击事件
        private:
               QPushButton *playBut;
               QSlider *volumeSlider;
               QSlider *slider;
               QPushButton *stopBut;
               QPushButton *muteBut;
               QPushButton *fsBut;


               //QGLWidget *videoWidget;

               QWidget *videoWidget;

               libvlc_instance_t *vlcInstance;
               libvlc_media_player_t *vlcPlayer;

               void initUI();
               void playList();
               void playNum(int num);
               QList<QString> VedioToPlayList;
               QString current_file;
               int ListId;
               int TotalVedio;

               bool port_open_flag;
               QSerialPort serial;
};


#endif
