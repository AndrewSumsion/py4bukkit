from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters

gateway = JavaGateway(start_callback_server=True, auto_convert=True)

server = gateway.entry_point.getBukkitServer()

listeners = {}

class Handler:
    def __init__(self, handler_method):
        self.handler_method = handler_method
    
    def handle(self, event):
        self.handler_method(event)
        gateway.entry_point.stopSync()

    class Java:
        implements = ["io.github.andrewsumsion.py4bukkit.PythonEventHandler"]

def on_event(event, handler_method):
    gateway.entry_point.registerEvent(event, Handler(handler_method))
