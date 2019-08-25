/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.forge;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.sampler.ThreadDumper;
import net.minecraft.command.ICommandSource;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ForgeSparkPlugin implements SparkPlugin {

    public static <T> void registerCommands(CommandDispatcher<T> dispatcher, Command<T> executor, String... aliases) {
        for (String alias : aliases) {
            LiteralArgumentBuilder<T> command = LiteralArgumentBuilder.<T>literal(alias)
                    .executes(executor)
                    .then(RequiredArgumentBuilder.<T, String>argument("args", StringArgumentType.greedyString())
                            .executes(executor)
                    );

            dispatcher.register(command);
        }
    }

    private final ForgeSparkMod mod;
    protected final ScheduledExecutorService scheduler;
    protected final SparkPlatform platform;

    protected ForgeSparkPlugin(ForgeSparkMod mod) {
        this.mod = mod;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("spark-forge-async-worker").build()
        );
        this.platform = new SparkPlatform(this);
        this.platform.enable();
    }

    abstract boolean hasPermission(ICommandSource sender, String permission);

    @Override
    public String getVersion() {
        return this.mod.getVersion();
    }

    @Override
    public Path getPluginDirectory() {
        return this.mod.getConfigDirectory();
    }

    @Override
    public void executeAsync(Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return new ThreadDumper.Specific(new long[]{Thread.currentThread().getId()});
    }
}
