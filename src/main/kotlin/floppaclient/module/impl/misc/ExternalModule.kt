package floppaclient.module.impl.dungeon


import floppaclient.FloppaClient.Companion.inDungeons
import floppaclient.FloppaClient.Companion.inSkyblock
import floppaclient.FloppaClient.Companion.mc
import floppaclient.module.Category
import floppaclient.module.Module
import floppaclient.module.SelfRegisterModule
import floppaclient.module.settings.impl.BooleanSetting
import floppaclient.module.settings.impl.NumberSetting
import floppaclient.module.AlwaysActive
import floppaclient.utils.ChatUtils
import floppaclient.utils.ChatUtils.modMessage
import net.minecraft.command.ICommandSender
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
    private val wdslot = NumberSetting("Slot to swap to", 0.0, 0.0, 9.0, 9.0, description = "Delay between clicks.")
    private val f7 = BooleanSetting("F/M7", default = false)
    private val f6 = BooleanSetting("F/M6", default = false)
    private val f5 = BooleanSetting("F/M5", default = false)
    private val f4 = BooleanSetting("F/M4", default = false)
    private val f3 = BooleanSetting("F/M3", default = false)
    private val f2 = BooleanSetting("F/M2", default = false)
    private val f1 = BooleanSetting("F/M1", default = false)
    private val bloodopen = BooleanSetting("Blood open", default = false)
    private var thread: Thread? = null

    init {
        this.addSettings(
            wdslot,
            f7,
            f6,
            f5,
            f4,
            f3,
            f2,
            f1,
            bloodopen
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
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!inDungeons || f7.enabled || event.type.toInt() == 2) return
        when (stripControlCodes(event.message.unformattedText)) {
            "[BOSS] Maxor WELL WELL WELL LOOK WHO'S HERE!" -> {
                modMessage("Detected in f7 bossfight, swapping armor")
                fun processCommand(sender: ICommandSender?, args: Array<String>) {
                    if (!inSkyblock) return
                    if (args.isEmpty()) {
                        modMessage("Specify slot")
                        return
                    }
                    val wdslot = args[0].toInt()
                    ChatUtils.sendChat("/wardrobe")
                    if (thread == null || !thread!!.isAlive) {
                        thread = Thread({
                            for (i in 0..100) {
                                if (mc.thePlayer.openContainer is ContainerChest && mc.thePlayer.openContainer?.inventorySlots?.get(
                                        0
                                    )?.inventory?.name?.startsWith("Wardrobe") == true
                                ) {
                                    mc.playerController.windowClick(
                                        mc.thePlayer.openContainer.windowId,
                                        35 + wdslot,
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
                        }, "Auto wardrobe")
                        return
                    }
                }
            }
        }
    }}