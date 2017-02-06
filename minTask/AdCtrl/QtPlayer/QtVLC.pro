TEMPLATE = app
TARGET = adplay
QT += opengl
CONFIG += serialport


DEPENDPATH += .
INCLUDEPATH += .
LIBS += -lvlc -lX11



# Input
HEADERS += player.h
SOURCES += main.cpp player.cpp
