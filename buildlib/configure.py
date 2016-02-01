import json
import hashlib
import os

from lumps import getlump

class Configuration(object):
    
    def __init__(self, directory):
    
        self.package = None
        self.rpgfile = None
        self.name = None
        self.icon_name = None    
        self.version = None
        self.numeric_version = None
        self.permissions = [ "VIBRATE" ]        
        self.layout = 'internal'
        self.expansion = False
        
        try:
            with file(os.path.join(directory, "config.json"), "r") as f:
                d = json.load(f)
            
            self.__dict__.update(d)            
        except:
            pass
        
    def save(self, directory):

        with file(os.path.join(directory, "config.json"), "w") as f:
            json.dump(self.__dict__, f)

def set_version(config, value):
    """
    Sets the version, and tries to set the numeric versions based on the 
    version number.
    """
    
    config.version = value
    
    try:
        v = 0
        
        for i in config.version.split('.'):
            v *= 100
            v += int(i)

        config.numeric_version = str(v)
    except:
        pass

def get_gamename(rpgfile):
    browse = getlump(rpgfile, 'BROWSE.TXT')
    size = ord(browse.read(1))
    browse.read(1)
    return browse.read(size)

def file_md5hash(filepath):
    "Compute MD5 hash of a file, returning a hex string"
    md5 = hashlib.md5()
    with open(filepath) as fil:
        md5.update(fil.read())
    return md5.hexdigest()

def configure(interface, directory):

    config = Configuration(directory)
    for filename in os.listdir(directory):
        if filename.split('.')[-1].lower() == 'rpg':
            config.rpgfile = filename
            break
    else:
        interface.fail("Please make sure that your project directory contains your .RPG file.")
    interface.info("Found RPG file: %s" % config.rpgfile)
    rpgfile = os.path.join(directory, config.rpgfile)
    config.rpg_hash = file_md5hash(rpgfile)
    config.name = get_gamename(rpgfile)
    interface.info("Found RPG Game file: %s" % config.name)
    config.name = interface.input("""What is the full name of your game? This name will appear in the list of installed applications.""", config.name)
    
    if config.icon_name is None:
        config.icon_name = config.name
    
    config.icon_name = interface.input("What is the short name of your game? This name will be used in the launcher, and for application shortcuts.", config.icon_name)

    config.package = interface.input("""\
What is the name of the package?

This is usually of the form com.domain.program or com.domain.email.program. It
must only contain ASCII letters and dots.""", config.package)

    configure_version(interface, config)

    #config.expansion = interface.choice("Would you like to create an expansion APK?", [
    #    (False, "No. Size limit of 50 MB on Google Play, but can be distributed through other store and sideloaded."),
    #    (True, "Yes. 2 GB size limit, but won't work outside of Google Play.")
    #    ], config.expansion)

    #config.layout = interface.choice("How is your application laid out?", [
    #        ("internal", "A single directory, that will be placed on device internal storage."),
    #        ("external", "A single directory, that will be placed on device external storage."),
    #        ("split", "Multiple directories that correspond to internal, external, and asset storage."),
    #        ], config.layout)

    permissions = " ".join(config.permissions)
    permissions = interface.input("""\
What permissions should your application have? Possible permissions include:

INTERNET (network access), VIBRATE (vibration control).
    
Please enter a space-separated list of permissions.""", permissions)
    config.permissions = permissions.split()

    config.save(directory)

    interface.write("Config complete. Run 'android.py build <project_dir>' to create your .apk")
    
def configure_version(interface, config):
    version = interface.input("""\
What is the application's version?

This should be the human-readable version that you would present to a person.""", config.version)
    set_version(config, version)
  
    config.numeric_version = interface.input("""What is the version code? 

This should be an integer number, and the value should increase between versions.""", config.numeric_version)    

def set_config(iface, directory, var, value):

    config = Configuration(directory)

    if var == "version":
        set_version(config, value)
    elif var == "permissions":
        config.permissions = value.split()
    elif hasattr(config, var):
        setattr(config, var, value)
    else:
        iface.fail("Unknown configuration variable: {}".format(var))
        
    config.save(directory)
        
def check_for_forced_update(iface, projectdir):
    "Check if the .rpg file has changed; if so need to bump version number"
    
    config = Configuration(projectdir)
    if config.package is None:
        iface.fail("Run configure before attempting to build the app.")

    rpgfile = os.path.join(projectdir, config.rpgfile)
    new_hash = file_md5hash(rpgfile)
    if new_hash != config.rpg_hash:
        old_version = config.numeric_version
        iface.write(config.rpgfile + " has changed, so it is mandatory to increase the version number.")
        configure_version(iface, config)
        if config.numeric_version <= old_version:
            iface.fail("That is not higher than the previous version number!")
        config.rpg_hash = new_hash
        config.save(projectdir)
