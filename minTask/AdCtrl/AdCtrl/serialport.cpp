#include "serialport.h"

SerialPort::SerialPort(QObject *parent) : QObject(parent)
{

    connect(&serial, SIGNAL(readyRead()), this, readData());
}

QStringList SerialPort::enumSeriPort()
{
    QStringList PortList;
    PortList.clear();

    foreach (const QSerialPortInfo &info, QSerialPortInfo::availablePorts())
    {
            PortList.append(info.portName());
    }

    return PortList;
}


int SerialPort::open_port(QString port)
{
    serialPortNum = port;
    serial.setPortName(serialPortNum);
    serial.setBaudRate(QSerialPort::Baud115200);
    serial.setDataBits(QSerialPort::Data8);
    serial.setParity(QSerialPort::NoParity);
    serial.setStopBits(QSerialPort::OneStop);
    serial.setFlowControl(QSerialPort::NoFlowControl);
    if (serial.open(QIODevice::ReadWrite))
    {
        return 0;
    }
    else
    {
        return -1;
    }
}

void SerialPort::close_port()
{
    if(serial.isOpen())
    {
        serial.close();
    }
}
