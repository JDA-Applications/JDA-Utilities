package com.jagrosh.jdautilities.commons.async;

import java.util.concurrent.CompletableFuture;

public class AsyncTask<T> extends CompletableFuture<T> implements AsyncFuture<T>
{
    @Override
    public CompletableFuture<T> toCompletableFuture()
    {
        throw new UnsupportedOperationException("Access to the CompletableFuture is not supported.");
    }
}
