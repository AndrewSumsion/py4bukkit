from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters

gateway = JavaGateway(start_callback_server=True, auto_convert=True)

server = gateway.entry_point.getBukkitServer()

class Handler:
    def __init__(self, handler_method):
        self.handler_method = handler_method
    
    def handle(self, event):
        self.handler_method(event)
        gateway.entry_point.stopSync()

    class Java:
        implements = ["io.github.andrewsumsion.py4bukkit.PythonEventHandler"]

class SyncTask:
    def __init__(self, task_method):
        self.task_method = task_method
    
    def run(self):
        self.task_method()
        gateway.entry_point.stopSync()
    
    class Java:
        implements = ["io.github.andrewsumsion.py4bukkit.PythonTask"]

class AsyncTask:
    def __init__(self, task_method):
        self.task_method = task_method
    
    def run(self):
        self.task_method()
    
    class Java:
        implements = ["io.github.andrewsumsion.py4bukkit.PythonTask"]

def on_event(event, handler_method):
    gateway.entry_point.registerEvent(event, Handler(handler_method))

def run_task(task_method, delay=0, asynchronous=False):
    if(asynchronous):
        gateway.entry_point.scheduleDelayedAsyncTask(AsyncTask(task_method), delay)
    else:
        gateway.entry_point.scheduleDelayedTask(SyncTask(task_method), delay)

def run_task_repeating(task_method, interval, delay=0, asynchronous=False):
    if(asynchronous):
        gateway.entry_point.scheduleDelayedRepeatingAsyncTask(AsyncTask(task_method), delay, interval)
    else:
        gateway.entry_point.scheduleDelayedRepeatingTask(SyncTask(task_method), delay, interval)