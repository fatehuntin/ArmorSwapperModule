package floppaclient.module.impl.dungeon


import floppaclient.FloppaClient.Companion.inDungeons
import floppaclient.FloppaClient.Companion.mc
import floppaclient.events.DungeonEndEvent
import floppaclient.floppamap.dungeon.Dungeon
import floppaclient.module.Category
import floppaclient.module.Module
import floppaclient.module.SelfRegisterModule
import floppaclient.module.settings.impl.BooleanSetting
import floppaclient.module.settings.impl.NumberSetting
import floppaclient.utils.ChatUtils
import floppaclient.utils.ChatUtils.modMessage
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


/**
 * A simple example of a Module written in Kotlin.
 *
 * In Kotlin Modules are declared as objects inheriting from the [Module] class.
 * The [SelfRegisterModule] annotation is required for this Module to be registered by the ModuleManager.
 * Specify the name, category and description of your module when delegating to the [Module] constructor.
 * Keep in mind that the name of your Module has to be unique!
 *
 * You can use this as a template for your own Modules.
 * The documentation of the members of this class should help you understand what everything does.
 *
 * Refer to the [Module] documentation for more information about Modules.
 * @author Aton
 */
@SelfRegisterModule
object ExternalModule : Module(
    "Auto armor swap",
    category = Category.DUNGEON,
    description = "A module to automatically swap armor sets on a chat message (only works w first page)"
) {
    /**
     * Here a Setting is added to your Module.
     *
     * This property is defined through delegation.
     *
     * **Remember to register your Setting**. Otherwise it will not appear in the GUI.
     * In this case this is done automatically by the provider of the delegate.
     * If you do not use delegation use the register method.
     *
     *     private val mySetting = register(BooleanSetting("My Setting"))
     * Or the operator form using +
     *
     *     private val mySetting = +BooleanSetting("My Setting")
     * You can remove or replace this Setting if you don't need it.
     */
    private val wdslot = NumberSetting("Slot to swap to", 1.0, 1.0, 9.0, 1.0, description = "Slot to swap to when entering boss")
    private val boss = BooleanSetting("Boss", default = false)
    private val bloodopen = BooleanSetting("Blood open", default = false)
    private val bloodopenslot = NumberSetting("Blood slot", 1.0, 1.0, 9.0, 1.0, description = "Slot to swap to when blood door opens")
    private val endofrun = BooleanSetting("End of run", default=false)
    private val endofrunslot = NumberSetting("End slot", 1.0, 1.0, 9.0, 1.0, description = "Slot to swap to when run ends")
    private var thread: Thread? = null
    init {
        this.addSettings(
            wdslot,
            boss,
            bloodopen,
            bloodopenslot,
            endofrun,
            endofrunslot
        )
    }
    /**
     * An Event listener for your Module.
     *
     * Methods annotated with [SubscribeEvent] are event listeners.
     * Such a method listens for the event specified by the Type of the single parameter of the method.
     * In this case that event is the Forge [ClientChatReceivedEvent].
     * Whenever that event occurs Forge will invoke this method and the code will be run.
     *
     * The event listeners in your Module will only be active when the Module is enabled.
     * You can make them always active by annotation the class with [AlwaysActive][floppaclient.module.AlwaysActive].
     */
    private var bloodOpened = false

    private var inboss = false
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!inDungeons || inboss) return
        if (Dungeon.inBoss) {
            val slot = wdslot.value.toInt()
            modMessage("Detected in bossfight, swapping armor")
            ChatUtils.sendChat("/wardrobe")
            if (thread == null || !thread!!.isAlive) {
                thread = Thread({
                    for (i in 0..100) {
                        if (mc.thePlayer.openContainer is ContainerChest && mc.thePlayer.openContainer?.inventorySlots?.get(0)?.inventory?.name?.startsWith("Wardrobe") == true) {
                            mc.playerController.windowClick(
                                mc.thePlayer.openContainer.windowId,
                                35+slot,
                                0,
                                0,
                                mc.thePlayer
                            )
                            mc.thePlayer.closeScreen()
                            return@Thread
                        }
                        Thread.sleep(20)
                    }
                    modMessage("§aWarobe failed, timed out")
                }, "Auto Wardrobe")
                thread!!.start()
                inboss = true
                return
            }
        }
    }
    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = stripControlCodes(event.message.unformattedText)
        if (!bloodopen.enabled || !inDungeons) return
        if (message == "The BLOOD DOOR has been opened!") {
            val slot = bloodopenslot.value.toInt()
            modMessage("Detected blood open, swapping armor")
            ChatUtils.sendChat("/wardrobe")
            if (thread == null || !thread!!.isAlive) {
                thread = Thread({
                    for (i in 0..100) {
                        if (mc.thePlayer.openContainer is ContainerChest && mc.thePlayer.openContainer?.inventorySlots?.get(0)?.inventory?.name?.startsWith("Wardrobe") == true) {
                            mc.playerController.windowClick(
                                mc.thePlayer.openContainer.windowId,
                                35+slot,
                                0,
                                0,
                                mc.thePlayer
                            )
                            mc.thePlayer.closeScreen()
                            return@Thread
                        }
                        Thread.sleep(20)
                    }
                    modMessage("§aWarobe failed, timed out")
                }, "Auto Wardrobe")
                thread!!.start()}
            bloodOpened = true
        } }
    @SubscribeEvent
    fun onDungeonEnd(event: DungeonEndEvent) {
        if (!inDungeons||!endofrun.enabled) return
        else {
            val slot = endofrunslot.value.toInt()
            modMessage("Detected run over, swapping armor")
            ChatUtils.sendChat("/wardrobe")
            if (thread == null || !thread!!.isAlive) {
                thread = Thread({
                    for (i in 0..100) {
                        if (mc.thePlayer.openContainer is ContainerChest && mc.thePlayer.openContainer?.inventorySlots?.get(0)?.inventory?.name?.startsWith("Wardrobe") == true) {
                            mc.playerController.windowClick(
                                mc.thePlayer.openContainer.windowId,
                                35+slot,
                                0,
                                0,
                                mc.thePlayer
                            )
                            mc.thePlayer.closeScreen()
                            return@Thread
                        }
                        Thread.sleep(20)
                    }
                    modMessage("§aWarobe failed, timed out")
                }, "Auto Wardrobe")
                thread!!.start()
    }}}
    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        if (!inDungeons) {
            bloodOpened = false
        }
    }
//Keep everything inside this
}