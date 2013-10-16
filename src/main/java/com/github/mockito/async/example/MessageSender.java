package com.github.mockito.async.example;

public final class MessageSender {

    private final MessageListener listener;

    public MessageSender(MessageListener listener) {
        this.listener = listener;
    }

    public void send(final Message message) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                listener.onMessage(message);
            }
        }).start();
    }
}
