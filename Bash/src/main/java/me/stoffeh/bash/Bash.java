package me.stoffeh.bash;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class Bash extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("[Bash] Bash has been enabled. Developer: StoffeH");
        this.getCommand("bash").setExecutor(new BashCommandExecutor());

        File files_folder = new File(getDataFolder(), "execute");
        if (!files_folder.exists()) {
            files_folder.mkdirs();
            getLogger().info("[Bash] Folders and configuration not found! Creating new files...");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("[Bash] Bash has been disabled :(");
    }

    public class BashCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length != 2) {
                sender.sendMessage("[Bash] Correct usage: /bash <filename> <delay>");
                return false;
            }
            String filename = args[0];
            File file = new File(getDataFolder(), "execute/" + filename + ".txt");
            if (!file.exists()) {
                sender.sendMessage("[Bash] File not found: " + filename + ".txt");
                return false;
            }
            int delay;
            try {
                delay = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("[Bash] Invalid delay amount: " + args[1]);
                return false;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder commands = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    commands.append(line).append("\n");
                }
                String[] commandLines = commands.toString().split("\n");

                new BukkitRunnable() {
                    int currentIndex = 0;

                    public void run() {
                        if (currentIndex >= commandLines.length) {
                            cancel();
                            return;
                        }
                        String commandToExecute = commandLines[currentIndex];
                        if (!commandToExecute.trim().isEmpty()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
                        }
                        currentIndex++;
                    }
                }.runTaskTimer(Bash.this, 0, delay);
                sender.sendMessage("[Bash] Commands from " + filename + ".txt will be executed with a delay of " + delay + " ticks between each command.");
            } catch (IOException e) {
                sender.sendMessage("[Bash] Error reading file " + filename + ".txt!");
            }
            return true;
        }
    }
}
