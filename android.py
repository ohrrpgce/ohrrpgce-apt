#!/usr/bin/env python2.7

import sys
sys.path.insert(0, 'buildlib/jinja2.egg')
sys.path.insert(0, 'buildlib')

import os
import argparse
import subprocess

import interface
import install_sdk
import configure
import build
import plat

def main():

    # Change into our root directory.
    ROOT = os.path.abspath(os.path.dirname(sys.argv[0]))
    os.chdir(ROOT)

    # Parse the arguments.
    ap = argparse.ArgumentParser(description="""
Package an OHRRPGCE game for Android. Start by running '%(prog)s install_sdk'.
Then create a 'project' directory for your game and put your .rpg file in it,
and run '%(prog)s configure <project_dir>' which will ask you questions
and create configuration files.
Finally run '%(prog)s build <project_dir>' to create the .apk.
    """)
    subparsers = ap.add_subparsers(dest='command', title='subcommands', description='Run "%(prog)s <subcommand> -h" to see options for each.')

    sub = subparsers.add_parser('install_sdk', help='Install Android SDK automatically.')

    sub = subparsers.add_parser('configure', help='Create config files.')
    sub.add_argument('project_dir', help='Project directory, containing an .rpg file.')

    sub = subparsers.add_parser('build', help='Create an .apk.')
    sub.add_argument('project_dir', help='Project directory, containing an .rpg file.')
    sub.add_argument('release_or_debug', choices=['release', 'debug'], default='release',
                     help='Project directory, containing an .rpg file.')

    sub = subparsers.add_parser('download', help='(UNIMPLEMENTED) Download OHRRPGCE build (normally done automatically).')
    sub.add_argument('stable', choices=['stable', 'nightly'], default='nightly') #, help='Whether to download latest stable release.')

    sub = subparsers.add_parser('setconfig', help='Modify a configuration value in the config.json file..')
    sub.add_argument('project_dir', help="Project directory (should have already run 'configure').")
    sub.add_argument('var', help="Variable to change. Possibilities include 'version', 'name', 'rpgfile', and more. See configure.py.")
    sub.add_argument('value', help='New value for the variable.')

    sub = subparsers.add_parser('logcat', help='Run adb logcat.')
    sub.add_argument('arguments', nargs='*', help='Extra adb arguments.')

    if len(sys.argv) == 1:
        ap.print_help()
        return

    args = ap.parse_args()

    iface = interface.Interface()

    if args.command == "install_sdk":
        install_sdk.install_sdk(iface)

    elif args.command == "configure":
        configure.configure(iface, args.project_dir)

    elif args.command == "setconfig":
        configure.set_config(iface, args.project_dir, args.var, args.value)

    elif args.command == "download":
        ap.error("download unimplemented")

    elif args.command == "build":
        configure.check_for_forced_update(iface, args.project_dir)
        build.build(iface, args.project_dir, args.release_or_debug)

    elif args.command == "logcat":
        subprocess.call([ plat.adb, "logcat", "-s", "python:*"] + args.arguments)

    else:
        # Should never happen
        ap.error("Unknown command: " + args.command)

if __name__ == "__main__":
    main()
