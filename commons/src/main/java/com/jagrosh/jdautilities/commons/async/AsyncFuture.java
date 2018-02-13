package com.jagrosh.jdautilities.commons.async;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public interface AsyncFuture<T> extends Future<T>, CompletionStage<T> {}
