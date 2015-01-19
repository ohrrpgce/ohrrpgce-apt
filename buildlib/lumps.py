from struct import *
from cStringIO import StringIO
import os

def getdata(f):
    tmp = ""
    while 1:
        byte = unpack('B',f.read(1))[0]
        if byte == 0:
            break
        tmp = tmp + chr(byte)
    return tmp

def getlong(f):
    d1 = f.read(2)
    d2 = f.read(2)
    return unpack('i',d2+d1)[0]

def unlump(game, dest):
    rpgdir = '%s/%s.rpgdir' % (dest, game.split('.')[0])
    os.mkdir(rpgdir)
    with open(game,'rb') as f:
        while 1:
            try:
                fi = getdata(f)
            except:
                break
            size = getlong(f)
            open('%s/%s' % (rpgdir,fi),'wb').write(f.read(size))

def getlump(game, lump):
    with open(game,'rb') as f:
        while 1:
            try:
                fi = getdata(f)
            except:
                break
            size = getlong(f)
            dat = f.read(size)
            if fi == lump:
                return StringIO(dat)
