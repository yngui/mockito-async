package com.github.mockito.async.example;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.github.mockito.async.Await.async;
import static com.github.mockito.async.Await.await;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

public class MessageSenderTest {

    private MessageListener listener;
    private MessageSender sender;
    private ArgumentCaptor<Message> messageCaptor;

    @Before
    public void setUp() throws Exception {
        listener = mock(MessageListener.class, async(withSettings().defaultAnswer(RETURNS_DEFAULTS)));
        sender = new MessageSender(listener);
        messageCaptor = ArgumentCaptor.forClass(Message.class);
    }

    @Test(timeout = 10000)
    public void testSend() throws Exception {
        Message message = new Message();
        sender.send(message);
        verify(listener, await()).onMessage(messageCaptor.capture());
        assertSame(message, messageCaptor.getValue());
    }
}
