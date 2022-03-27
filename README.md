
# OHRRPGCE Android Packaging Tool (OHRRPGCEAPT)

This is an unfinished tool for packaging OHRRPGCE games for Android as .apks in a much easier way than the standard, extremely convoluted, way used by the OHRRPGCE developers. But it is not yet recommended, and no games are known to have been released in the wild using this tool.

Currently it includes a precompiled copy of the OHRRPGCE from 2016. It needs to be modified to download official up-to-date builds. Also, it creates a keystore but does not sign the .apk which you would have to do manually in order to be able to submit to app stores.

It requires the Android SDK and Apache Ant, which it downloads automatically, as well as Python.

It was originally created by Kevin Veroneau heavily based upon [Pygame Subset for Android/Ren'Py Android Packaging Tool](https://github.com/renpy/rapt) by Tom Rothamel and Patrick Dawson (see also the [pgs4a fork](https://github.com/startgridsrc/pgs4a)), which in turn is based upon [Python-for-android](https://github.com/kivy/python-for-android).

# Usage

[These notes](https://www.slimesalad.com/forum/viewtopic.php?p=111978#p111978), although very old, may still be relevant.

# License
The Pygame Subset for Android is licensed under the GNU Lesser General Public License. To the best of our knowledge, Pygame, SDL, and all other dependences are licensed under compatible licenses.

The Pygame Subset for Android is by:

Tom Rothamel
Patrick Dawson
It integrates code from a number of projects under other licenses:

Python-for-android
Pelya's Android port of SDL 1.2 ([OHRRPGCE fork](https://github.com/bob-the-hamster/commandergenius))
Jinja2
Colorama
Binary builds of the OHRRPGCE, SDL and SDL_mixer
