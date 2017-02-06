#ifndef SERIALPORT_H
#define SERIALPORT_H

#include <QObject>
#include <QString>
#include <QStringList>
#include <QtSerialPort/QSerialPort>
#include <QtSerialPort/QSerialPortInfo>
//#include <QSerialPort>
//#include <QSerialPortInfo>



class SerialPort : public QObject
{
    Q_OBJECT
public:
    explicit SerialPort(QObject *parent = 0);
    QStringList enumSeriPort();
    int open_port(QString port);
    void close_port();
signals:

public slots:
    void readData();
private:
    QString serialPortNum;
    QSerialPort serial;
};

#endif // SERIALPORT_H
