package org.e2d3;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

public class HackathonApplication extends WebApplication {

    @Override
    protected void init() {
        super.init();

        getDebugSettings().setAjaxDebugModeEnabled(false);

        mountPage("/edit", EditTeamPage.class);
        mountPage("/login", LoginPage.class);
        mountPage("/result", ResultPage.class);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new UserData(request);
    }

}
