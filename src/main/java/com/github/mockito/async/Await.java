/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Igor Konev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.mockito.async;

import org.mockito.MockSettings;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.verification.ArgumentsAreDifferent;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.times;

public final class Await implements VerificationMode {

    private final VerificationMode delegate;

    public Await() {
        this(times(1));
    }

    public Await(VerificationMode delegate) {
        this.delegate = delegate;
    }

    @Override
    public void verify(VerificationData data) {
        Object mock = data.getWanted().getInvocation().getMock();
        synchronized (mock) {
            while (true) {
                try {
                    delegate.verify(data);
                    return;
                } catch (ArgumentsAreDifferent ignored) {
                } catch (TooLittleActualInvocations ignored) {
                } catch (WantedButNotInvoked ignored) {
                }
                try {
                    mock.wait();
                } catch (InterruptedException ignored) {
                    throw new MockitoAssertionError("interrupted");
                }
            }
        }
    }

    public static VerificationMode await() {
        return new Await();
    }

    public static MockSettings async(MockSettings settings) {
        return settings.invocationListeners(new InvocationListener() {

            @Override
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
                DescribedInvocation invocation = methodInvocationReport.getInvocation();
                if (invocation instanceof InvocationOnMock) {
                    Object mock = ((InvocationOnMock) invocation).getMock();
                    synchronized (mock) {
                        mock.notifyAll();
                    }
                }
            }
        });
    }
}
