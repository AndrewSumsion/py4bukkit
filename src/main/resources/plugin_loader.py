import os
import sys
import importlib
import time

sys.path.append("plugins/PythonPlugins")
import minecraft

plugin_folder = sys.argv[1]
sys.path.append(plugin_folder)

for filename in os.listdir(plugin_folder):
    if filename.endswith(".py"):
        importlib.import_module(filename.replace(".py", ""))