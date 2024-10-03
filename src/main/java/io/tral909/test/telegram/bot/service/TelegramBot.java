package io.tral909.test.telegram.bot.service;

import com.vdurmont.emoji.EmojiParser;
import io.tral909.test.telegram.bot.model.Ads;
import io.tral909.test.telegram.bot.model.AdsRepository;
import io.tral909.test.telegram.bot.model.User;
import io.tral909.test.telegram.bot.model.UserRepository;
import io.tral909.test.telegram.bot.properties.BotProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private static final String HELP_TEXT = "This bot is created to demonstrate Spring and telegrambots library capabilities.\n\n"
            + "You can execute commands from the main menu on the left or by typing "
            + "Type /start to see welcome message"
            + "Type /mainvalutes to see main exchange rates";
    //todo add all commands description

    private static final String YES_BUTTON = "YES_BUTTON";
    private static final String NO_BUTTON = "NO_BUTTON";
    private static final String ERROR_TEXT = "Error occurred: ";

    private final BotProperties botProperties;
    private final CbrService cbrService;
    private final UserRepository userRepository;
    private final AdsRepository adsRepository;

    @Autowired
    public TelegramBot(BotProperties botProperties, CbrService cbrService, UserRepository userRepository, AdsRepository adsRepository) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.cbrService = cbrService;
        this.userRepository = userRepository;
        this.adsRepository = adsRepository;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "get a welcome message"));
        commands.add(new BotCommand("/mydata", "get your data stored"));
        commands.add(new BotCommand("/deletedata", "delete my data"));
        commands.add(new BotCommand("/help", "info how to use this bot"));
        commands.add(new BotCommand("/settings", "set your preferences"));
        commands.add(new BotCommand("/mainvalutes", "get BYN USD EUR exchange rates"));

        try {
            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
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

            if (messageText.contains("/send") && Objects.equals(botProperties.getOwnerId(), chatId)) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    prepareAndSendMessage(user.getChatId(), textToSend);
                }
                return;
            }

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

                    prepareAndSendMessage(chatId, respText);
                    break;

                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;

                case "/register":
                    register(chatId);
                    break;

                case "/send":
                    log.info("/send command was called by simple user");
                    prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                    break;

                default:
                    prepareAndSendMessage(chatId, "Sorry, command was not recognized");
            }
        // Если id кнопки передался от юзера при нажатии, то тут обрабатывам
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            String text = null;
            if (callbackData.equals(YES_BUTTON)) {
                text = "You presses YES button";
            } else if (callbackData.equals(NO_BUTTON)) {
                text = "You presses NO button";
            }
            executeMessageEditText(text, chatId, messageId);
        }
    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);
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

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        row.add("/mainvalutes");
        row.add("/help");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("/register");
        row.add("check my data");
        row.add("delete my data");
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        // to hide keyboard once its clicked
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        executeMessage(sendMessage);
    }

    private void executeMessageEditText(String text, long chatId, int messageId) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);

        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds() {
        var ads = adsRepository.findAll();
        var users = userRepository.findAll();

        for (Ads ad : ads) {
            for (User user : users) {
                prepareAndSendMessage(user.getChatId(), ad.getAd());
            }
        }
    }
}
