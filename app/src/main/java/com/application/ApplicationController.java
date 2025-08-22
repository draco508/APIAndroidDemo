// Copyright 2012 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.application;

import android.app.Application;

import com.server.AppWebServer;
import com.util.Utils;

public class ApplicationController extends Application {


    private static ApplicationController singleton;

    public static ApplicationController getInstance() {
        return singleton;
    }

    public int hostPort;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Application.getProcessName().contains(":")) {
            singleton = this;
            hostPort = Utils.findFreePort(-1);
            AppWebServer.startServer();

        }

    }


}
