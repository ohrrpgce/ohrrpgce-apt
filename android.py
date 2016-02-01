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
    ap = argparse.ArgumentParser(description="Build an android package.")
    ap.add_argument("command", help="The command to run. One of install_sdk, configure, or build.")
    ap.add_argument("argument", nargs='*', help="The arguments to the selected command.")
    
    args = ap.parse_args()

    iface = interface.Interface()
    
    def check_args(n, whatargs = "."):
        if len(args.argument) != n:
            iface.fail("The {} command expects {} arguments{}".format(args.command, n, whatargs))
            
        return args.argument
    
    if args.command == "install_sdk":
        check_args(0)
        install_sdk.install_sdk(iface)
        
    elif args.command == "configure":
        directory, = check_args(1, ": android.py configure <project_directory>")
        configure.configure(iface, directory)
        
    elif args.command == "setconfig":
        directory, var, value = check_args(3)
        configure.set_config(iface, directory, var, value)
        
    elif args.command == "build":
        projectdir = args.argument[0]
        if len(args.argument) < 2:
            iface.fail("The build command expects at least 2 arguments:\n\n android.py build <project_directory> {release|debug}")
        build.build(iface, projectdir, args.argument[1:])

    elif args.command == "logcat":
        subprocess.call([ plat.adb, "logcat", "-s", "python:*"] + args.argument)
        
    else:
        ap.error("Unknown command: " + args.command)
        
if __name__ == "__main__":
    main()
