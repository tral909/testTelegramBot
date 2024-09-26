package io.tral909.test.telegram.bot.service;

import com.vdurmont.emoji.EmojiParser;
import io.tral909.test.telegram.bot.model.User;
import io.tral909.test.telegram.bot.model.UserRepository;
import io.tral909.test.telegram.bot.properties.BotProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private static final String HELP_TEXT = "This bot is created to demonstrate Spring and telegrambots library capabilities.\n\n"
            + "You can execute commands from the main menu on the left or by typing "
            + "Type /start to see welcome message"
            + "Type /mainvalutes to see main exchange rates";
    //todo add all commands description

    private final BotProperties botProperties;
    private final CbrService cbrService;
    private final UserRepository userRepository;

    @Autowired
    public TelegramBot(BotProperties botProperties, CbrService cbrService, UserRepository userRepository) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.cbrService = cbrService;
        this.userRepository = userRepository;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "get a welcome message"));
        commands.add(new BotCommand("/mydata", "get your data stored"));
        commands.add(new BotCommand("/deletedata", "delete my data"));
        commands.add(new BotCommand("/help", "info how to use this bot"));
        commands.add(new BotCommand("/settings", "set your preferences"));
        commands.add(new BotCommand("/mainvalutes", "get BYN USD EUR exchange rates"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText();
            Long chatId = message.getChatId();

            switch (messageText) {
                case "/start":
                    //todo uncomment when we want to register users
                    //registerUser(message);
                    startCommandReceived(chatId, message.getChat().getFirstName());
                    break;

                case "/mainvalutes":
                    // due to 400 error from tg - too long message (for long valutes list from cbr)
                    // return main 3 valutes

                    var response = cbrService.getExchangeRatesMainValutes();
                    var respText = "Курс валют на " + response.getDate() + "\n\n" +
                            response.getValute().stream()
                            .map(v -> v.getCharCode() + "\n" + v.getName() + "\n" + v.getValue())
                            .collect(Collectors.joining("\n\n"));

                    sendMessage(chatId, respText);
                    break;

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                default:
                    sendMessage(chatId, "Sorry, command was not recognized");
            }
        }
    }

    private void registerUser(Message message) {

        if (userRepository.findById(message.getChatId()).isEmpty()) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you! :wave:");
        /*String answer = "Hi, " + name + ", nice to meet you!"; //"\n\n" +
                /*"""
                You can use next commands:
                /mainvalutes - get BYN USD EUR exchange rates
                """;*/
        sendMessage(chatId, answer);
    }

    @SneakyThrows
    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        execute(sendMessage);
    }
}
