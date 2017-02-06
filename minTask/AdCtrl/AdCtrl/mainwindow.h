#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QtSerialPort/QSerialPort>
#include <QtSerialPort/QSerialPortInfo>
#include <QMessageBox>
#include <QDebug>
#include <QAction>

#include "msgid.h"

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

    QStringList enumSeriPort();
    int open_port(QString port);
    void close_port();

private slots:
    void on_pushButton_serialPort_clicked();
    void readData();
    void on_pushButton_getCurrentFile_clicked();

    void on_pushButton_getVideoList_clicked();



    void on_pushButton_play_after_clicked();

    void on_pushButton_play_before_clicked();

    void on_pushButton_pause_clicked();

    void on_pushButton_close_clicked();



    void on_pushButton_play_target_vedio_clicked();

    void on_pushButto_play_clicked();

private:
    Ui::MainWindow *ui;
    QStringList uartStringList;
    bool port_open_flag;
    QSerialPort serial;

    char recvBufer[4098];
    char fullMsgBuf[256];      /* 假定最大包长度为5M */
    void handlCompleteData();
    int point;
    int startFrame;
    int startPoint;
    long long totalRecved;
    long long msgLenth;
};

#endif // MAINWINDOW_H
