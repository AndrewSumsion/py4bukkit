import minecraft

def handle_sneak(event):
    minecraft.gateway.entry_point.getBukkitServer().broadcastMessage("derp")

minecraft.on_event("PlayerToggleSneakEvent", handle_sneak)