from py4j.java_gateway import JavaGateway, GatewayParameters

gateway = JavaGateway(auto_convert=True)
gateway.start_callback_server()

server = gateway.entry_point.getBukkitServer()

listeners = {}

class Handler:
    def __init__(self, handler_method):
        self.handler_method = handler_method
    
    def handle(self, event):
        self.handler_method(event)

    class Java:
        implements = ["io.github.andrewsumsion.pythonplugins.PythonEventHandler"]

def on_event(event, handler_method):
    gateway.entry_point.subscribe(event)
    if listeners.get(event) != None:
        listeners.get(event).append(Handler(handler_method))
    else:
        listeners[event] = [Handler(handler_method)]

def start_event_loop():
    while True:
        if gateway.entry_point.eventAvailable():
            event = gateway.entry_point.takeEvent()
            event_name = event.getClass().getSimpleName()
            for key in listeners:
                if key == event_name:
                    for handler in listeners.get(key):
                        gateway.entry_point.handleSynchronously(event, handler)