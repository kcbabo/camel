/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.processor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.concurrent.ExecutorServiceHelper;

/**
 * Async processor that turns the processing going forward into async mode.
 * <p/>
 * The original caller thread will receive a <tt>Future&lt;Exchange&gt;</tt> in the OUT message body.
 * It can then later use this handle to obtain the async response.
 * <p/>
 * Camel also provides type converters so you can just ask to get the desired object type and Camel
 * will automatic wait for the async task to complete to return the response.
 *
 * @version $Revision$
 */
public class AsyncProcessor extends DelegateProcessor implements Processor {

    private static final int DEFAULT_THREADPOOL_SIZE = 5;
    private ExecutorService executorService;
    private boolean waitTaskComplete;

    public AsyncProcessor(Processor output, ExecutorService executorService, boolean waitTaskComplete) {
        super(output);
        this.executorService = executorService;
        this.waitTaskComplete = waitTaskComplete;
    }

    public void process(final Exchange exchange) throws Exception {
        final Processor output = getProcessor();
        if (output == null) {
            // no output then return
            return;
        }

        // use a new copy of the exchange to route async
        final Exchange copy = exchange.newCopy();

        // let it execute async and return the Future
        Callable<Exchange> task = new Callable<Exchange>() {
            public Exchange call() throws Exception {
                // must use a copy of the original exchange for processing async
                output.process(copy);
                return copy;
            }
        };

        // sumbit the task
        Future<Exchange> future = getExecutorService().submit(task);

        // TODO: Support exchange headers for wait and timeout values, see Exchange constants

        if (waitTaskComplete) {
            // wait for task to complete
            Exchange response = future.get();
            // if we are out capable then set the response on the original exchange
            if (ExchangeHelper.isOutCapable(exchange)) {
                ExchangeHelper.copyResults(exchange, response);
            }
        } else {
            // no we do not expect a reply so lets continue, set a handle to the future task
            // in case end user need it later
            exchange.getOut().setBody(future);
        }
    }

    public ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = createExecutorService();
        }
        return executorService;
    }

    private ExecutorService createExecutorService() {
        return ExecutorServiceHelper.newScheduledThreadPool(DEFAULT_THREADPOOL_SIZE, "AsyncProcessor", true);
    }

    protected void doStop() throws Exception {
        super.doStop();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

}
