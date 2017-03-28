package com.sample.telegram_bot;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.api.methods.ActionType;
import org.telegram.telegrambots.api.methods.send.SendChatAction;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiValidationException;

public class MyAmazingBot extends TelegramLongPollingBot {

    private static final String KB_PREFIX             = "# ";

    private static final String KB_NEW_POLICY_HOME    = KB_PREFIX + "Buy home insurance policy";
    private static final String KB_NEW_POLICY_CAR     = KB_PREFIX + "Buy car insurance policy";
    private static final String KB_NEW_POLICY_LIFE    = KB_PREFIX + "Buy life insurance policy";

    private static final String KB_NEW_POLICY_AGE_1   = KB_PREFIX + "Below 25 years";
    private static final String KB_NEW_POLICY_AGE_2   = KB_PREFIX + "25-35 years";
    private static final String KB_NEW_POLICY_AGE_3   = KB_PREFIX + "Above 35 years";

    private static final String KB_NEW_POLICY_CONTACT = KB_PREFIX + "Send contact number";

    private boolean             connectedToAgent      = false;
    private static Long         chatId                = -1L;

    public String getBotUsername() {
        return Config.BOT_NAME;
    }

    private String getMessage_system(String msg) {
        //return "<i>" + msg + "</i>";
        return msg;
    }

    @Override
    public String getBotToken() {
        return Config.BOT_TOKEN;
    }

    public void onUpdateReceived(Update update) {
        System.out.println("**** " + update);
        try {
            // We check if the update has a message and the message has text
            if (update.hasMessage() && update.getMessage().hasText()) {
                if (chatId < 0)
                    chatId = update.getMessage().getChatId();
                handleMessage(update);
            }
            else if (update.hasMessage() && update.getMessage().getContact() != null) {
                sendMessage_custom("Thanks for sharing contact number, we will call you back shortly for further process...");
            }
            else if (update.hasCallbackQuery()) {
                if (chatId < 0)
                    chatId = update.getCallbackQuery().getMessage().getChatId();
                handleCallbackQuery(update);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException, InterruptedException {

        if (update.getCallbackQuery().getData().equals(Declarations.COMMAND_DONT_CONNECT_AGENT)) {
            editMessageText(initCallbackMessage(update)
                    .setText("Agent connection cancelled. You can explore self service command by pressing '/'. \n\nTo connect to backoffice agent anytime, send following message  \n /connectagent")
                    .setParseMode("html"));

        }
        else if (update.getCallbackQuery().getData().equals(Declarations.COMMAND_CONNECT_AGENT)) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            //this is thread simulation to connect backoffice agent, move to separat thread later
            editMessageText(initCallbackMessage(update).setText(getMessage_system("Connecting to next available agent, please wait...")).setParseMode("html"));

            //sendMessage_custom(chatId, "Connecting to next available agent, please wait...");
            Thread.sleep(1000 * 3);
            sendMessage_custom("All backoffice agents are busy at the moment, please wait...");
            Thread.sleep(1000 * 3);
            sendMessage_custom("Agent assigned, connecting...");
            sendMessage_fromAgent("Hello, how can I help you?");

            connectedToAgent = true;
        }
        else {
            editMessageText(initCallbackMessage(update)
                    .setText("Received and handled user command :: " + update.getCallbackQuery().getData()));
        }
    }

    private void handleMessage(Update update) throws TelegramApiException {
        String incomingMessage = update.getMessage().getText();
        if (incomingMessage != null) {
            incomingMessage = incomingMessage.trim();

            if (incomingMessage.startsWith(KB_PREFIX)) {
                handleKeyboardInputs(incomingMessage);
            }
            else if (incomingMessage.equalsIgnoreCase("/help")) {
                sendMessage_help();
            }
            else if (incomingMessage.equalsIgnoreCase("/connectagent")) {
                if (connectedToAgent) {
                    sendMessage_custom("You are already connected to an agent...");
                }
                else {
                    sendMessage_connectAgentCommand();
                }
            }
            else if (incomingMessage.equalsIgnoreCase("/start")) {
                sendMessage_start();
            }
            else if (incomingMessage.equalsIgnoreCase("/newpolicy")) {
                sendMessage_newPolicy();
            }
            else if (incomingMessage.equalsIgnoreCase("/test-typing")) {
                sendChatAction(new SendChatAction()
                        .setChatId(update.getMessage().getChatId())
                        .setAction(ActionType.TYPING));
            }
            else if (incomingMessage.equalsIgnoreCase("/test-video")) {
                sendChatAction(new SendChatAction()
                        .setChatId(update.getMessage().getChatId())
                        .setAction(ActionType.RECORDVIDEO));
            }
            else if (incomingMessage.equalsIgnoreCase("/test-file")) {
                sendChatAction(new SendChatAction()
                        .setChatId(update.getMessage().getChatId())
                        .setAction(ActionType.UPLOADDOCUMENT));
            }
            else {
                if (connectedToAgent) {
                    sendMessage_fromAgent("this is manually typed message from agent... Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt");
                }
                else
                {
                    sendMessage_connectAgentCommand();
                }
            }
        }
    }

    private void handleKeyboardInputs(String incomingMessage) throws TelegramApiException {
        System.out.println("&******** " + incomingMessage);
        if (incomingMessage.equals(KB_NEW_POLICY_CAR) || incomingMessage.equals(KB_NEW_POLICY_HOME) || incomingMessage.equals(KB_NEW_POLICY_LIFE)) {
            sendMessage_newPolicy_age();
        }
        else if (incomingMessage.equals(KB_NEW_POLICY_AGE_1) || incomingMessage.equals(KB_NEW_POLICY_AGE_2) || incomingMessage.equals(KB_NEW_POLICY_AGE_3)) {
            sendMessage_newPolicy_contact();
        }
    }

    private void sendMessage_help() throws TelegramApiException {
        sendMessage(initMessage()
                .setText("Happy to help you! You can use any of the following commands: \n\n[TODO- commands list]"));

    }

    private void sendMessage_custom(String msg) throws TelegramApiException {
        sendMessage(initMessage()
                .setText(getMessage_system(msg))
                .setParseMode("html"));
    }

    private void sendMessage_fromAgent(String msg) throws TelegramApiException {
        sendMessage(initMessage()
                .setText("<b>[Agent: Stefan Kaiser]</b> \n" + msg)
                .setParseMode("html"));
    }

    private void sendMessage_connectAgentCommand() throws TelegramApiException {
        sendMessage(initMessage()
                .setText(getMessage_system("Hello, do you wish to connect to backoffice agent?"))
                .setParseMode("html")
                .setReplyMarkup(getHelpKeyboardConnectAgent()));
    }

    private void sendMessage_newPolicy() throws TelegramApiException {
        sendMessage(initMessage()
                .setText(getMessage_system("Please choose type of policy you want to purchase"))
                .setReplyMarkup(getHelpKeyboard_newPolicyType())
                .setParseMode("html"));
    }

    private void sendMessage_newPolicy_age() throws TelegramApiException {
        sendMessage(initMessage()
                .setText(getMessage_system("Please select your age"))
                .setReplyMarkup(getHelpKeyboard_newPolicyType_age())
                .setParseMode("html"));
    }

    private void sendMessage_newPolicy_contact() throws TelegramApiException {
        sendMessage(initMessage()
                .setText(getMessage_system("Please share your contact number with us"))
                .setReplyMarkup(getHelpKeyboard_newPolicyType_contact())
                .setParseMode("html"));
    }

    private void sendMessage_start() throws TelegramApiException {
        sendMessage(initMessage()
                .setText(getMessage_system("Welcome to XYZ Bank Customer Service! \nDo you wish to connect to a backoffice agent?"))
                .setReplyMarkup(getHelpKeyboardConnectAgent())
                .setParseMode("html"));
    }

    private ReplyKeyboardMarkup getHelpKeyboard_newPolicyType() {
        List<KeyboardRow> result = new ArrayList<KeyboardRow>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(KB_NEW_POLICY_HOME));
        result.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton(KB_NEW_POLICY_LIFE));
        result.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton(KB_NEW_POLICY_CAR));
        result.add(row);

        return new ReplyKeyboardMarkup()
                .setOneTimeKeyboad(true)
                .setKeyboard(result).setResizeKeyboard(true);
    }

    private ReplyKeyboardMarkup getHelpKeyboard_newPolicyType_age() {
        List<KeyboardRow> result = new ArrayList<KeyboardRow>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(KB_NEW_POLICY_AGE_1));
        result.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton(KB_NEW_POLICY_AGE_2));
        result.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton(KB_NEW_POLICY_AGE_3));
        result.add(row);

        return new ReplyKeyboardMarkup()
                .setOneTimeKeyboad(true)
                .setKeyboard(result).setResizeKeyboard(true);
    }

    private ReplyKeyboardMarkup getHelpKeyboard_newPolicyType_contact() {
        List<KeyboardRow> result = new ArrayList<KeyboardRow>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton btn = new KeyboardButton(KB_NEW_POLICY_CONTACT);
        btn.setRequestContact(true);
        row.add(btn);
        result.add(row);

        return new ReplyKeyboardMarkup()
                .setOneTimeKeyboad(true)
                .setKeyboard(result)
                .setResizeKeyboard(true);
    }

    private InlineKeyboardMarkup getHelpKeyboardConnectAgent() {
        List<InlineKeyboardButton> result = new ArrayList<InlineKeyboardButton>();

        result.add(new InlineKeyboardButton().setText("Connect to agent").setCallbackData(Declarations.COMMAND_CONNECT_AGENT));
        result.add(new InlineKeyboardButton().setText("No thanks").setCallbackData(Declarations.COMMAND_DONT_CONNECT_AGENT));

        List<List<InlineKeyboardButton>> keys = new ArrayList<List<InlineKeyboardButton>>();
        keys.add(result);

        return new InlineKeyboardMarkup()
                .setKeyboard(keys);
    }

    private SendMessage initMessage() {
        return new SendMessage().setChatId(chatId);
    }

    private EditMessageText initCallbackMessage(Update update) {
        return new EditMessageText()
                .setChatId(chatId)
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        // return null;
    }
}
