package rmd.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rmd.date.Time;
import rmd.errors.Exceptions;
import rmd.reminding.Reminding;
import rmd.sequelize.Select;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;

public class GuildMessageUpcoming extends ListenerAdapter {
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Reminding.prefix + "rmd")
                && args.length>=2
                && args[1].equalsIgnoreCase("upcoming")) {
            EmbedBuilder info = new EmbedBuilder();

            String argumento;

            boolean noError = false;
            String role = null;
            int messagesLength = 0;

            Long serverID = Long.parseLong(event.getGuild().getId());
            //Long channelID = Long.parseLong(event.getChannel().getId());
            //Future implementation for a best message filter

            try {
                try {
                    argumento = args[2];
                } catch (ArrayIndexOutOfBoundsException e) {
                    argumento = "ArrayIndexOutOfBoundsException";
                }
                if(argumento.contains("ArrayIndexOutOfBoundsException")) {
                    //Without a second arg
                    String[][] messages = null;
                    try {
                        messages = Select.selectMessages(serverID);
                        messagesLength = messages.length;
                    } catch (NullPointerException e) {
                        argumento = "NullPointerException";
                        info.addField("ERROR:", "Não existe nenhum evento cadastrado neste servidor!", false);
                        info.setColor(0xff0000);
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                    if (!argumento.contains("NullPointerException")) {
                        for(int i=0; i < messagesLength; i++) {
                            if(!messages[i][3].contains("everyone")) {
                                role = "<@&" + messages[i][3] + ">";
                                break;
                            }
                        }

                        for (int i = 0; i < messagesLength; i++) {
                            int numero = i + 1;
                            info.setTitle("📚 RemindingBot: Evento por vir ⏰\n"
                                    + "-------------------------------------------");
                            if (messages[i][1]!=null) {
                                messages[i][1] = rmd.date.Time.timeLeft(messages[i][1]);
                            }

                            for (int j=0; j<2; j++) {
                                if(messages[i][j]==null) {
                                    messages[i][j]="Não informado!";
                                }
                            }
                            info.addField("Evento " + numero + ": " + messages[i][0],
                                    "Tempo restante :\n" + messages[i][1] +
                                            "\nID: " + messages[i][2], false);
                        }

                        info.setFooter("Criado por : Andrew Medeiros & Brayan Amaral");
                        noError = true;
                    }
                } else {
                    //With a secondary arg (messageID)
                    String[] message = Select.select(Long.parseLong(args[2]) ,serverID);
                    if(message[3]==null) {
                        info.addField("ERROR :", "Não existe nenhum evento com esse ID("+args[2]+") neste servidor!", false);
                        info.setColor(0xff0000);
                    } else {
                        try {
                            String daysLeft = "Data final não informada!";
                            String title, date, description;
                            if (message[1]!=null) {
                                daysLeft = Time.timeLeft(message[1]);
                            }
                            for (int j=0; j<4; j++) {
                                if(message[j]==null) {
                                    message[j]="Não informado!";
                                }
                            }
                            title = message[0];
                            date = message[1];
                            description = message[2];

                            info.setTitle("📚  RemindingBot: Evento por vir  ⏰\n"
                                    + "-------------------------------------------");
                            info.addField("Evento :", title, false);
                            info.addField("Data final :", date, false);
                            info.addField("Descrição :", description, false);
                            info.addField("Tempo restante :", daysLeft, false);
                            info.addField("ID : " + args[2],"", false);
                            if (title.contains("Não informado!") && date.contains("Não informado!") && description.contains("Não informado!")) {
                                info.addField("Aviso :", "Este evento pode ser deletado em menos \nde um dia por falta de informações!", false);
                            }
                            info.setFooter("Criado por : " + message[3]);
                            info.setColor(0x2d3b7a);

                        } catch (NullPointerException | ParseException e) {
                            if (e.toString().contains("NullPointerException")) {
                                info = Exceptions.idNotFound(args[2]);
                            } else {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                event.getChannel().sendMessageEmbeds(info.build()).queue();
                if(noError && role!=null) {
                    event.getChannel().sendMessage(role).queue();
                }
                info.clear();
            } catch (SQLException | NumberFormatException | ParseException | IOException | URISyntaxException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                if (e.toString().contains("NullPointerException")) {
                    info = Exceptions.idNotFound(args[2]);
                } else if(e.toString().contains("SQLException")) {
                    info = Exceptions.sqlConnection();
                }
                event.getChannel().sendMessageEmbeds(info.build()).queue();
                info.clear();
            }
        }
    }
}
