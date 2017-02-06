#-------------------------------------------------
#
# Project created by QtCreator 2016-10-18T10:07:48
#
#-------------------------------------------------

greaterThan(QT_MAJOR_VERSION, 4) {
    QT       += widgets
} else {
    include($$QTSERIALPORT_PROJECT_ROOT/src/serialport/qt4support/serialport.prf)
}

RC_FILE = ico.rc

CONFIG += serialport

TARGET = AdCtrl
TEMPLATE = app


SOURCES += main.cpp\
        mainwindow.cpp \

HEADERS  += mainwindow.h \
    msgid.h

FORMS    += mainwindow.ui
