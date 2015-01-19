from struct import *
from cStringIO import StringIO
import os, zipfile

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

def lump2zip(game, dest):
    rpgdir = '%s.rpgdir' % os.path.basename(game).split('.')[0]
    zf = zipfile.ZipFile(os.path.join(dest, 'gamedata.zip'), 'w')
    zf.writestr('ohrrpgce_arguments.txt', rpgdir)
    with open(game,'rb') as f:
        while 1:
            try:
                fi = getdata(f)
            except:
                break
            size = getlong(f)
            zf.writestr('%s/%s' % (rpgdir,fi), f.read(size))

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
