package com.bxkcli.browser;

public interface BrowserConnector {
    String status();

    String connectDefault();

    String disconnect();
}
