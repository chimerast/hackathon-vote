package org.e2d3;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

public class LoginPage extends WebPage {

    private String userId;
    private String teamId;

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Form<?> loginForm = new Form<Object>("loginForm") {
            @Override
            protected void onSubmit() {
                User user = DAO.getUser(userId);
                if (user == null) {
                    user = new User();
                    user.id = userId;
                }
                user.teamId = teamId;

                DAO.save(user);

                UserData.get().setUserId(user.id);

                setResponsePage(HomePage.class);
            }
        };

        loginForm.setDefaultModel(new CompoundPropertyModel<LoginPage>(this));

        add(loginForm);

        List<String> teams = new ArrayList<String>();
        for (int i = 0; i < 20; ++i) {
            teams.add(Character.toString((char) ('A' + i)));
        }

        loginForm.add(new TextField<String>("userId").setRequired(true));
        loginForm.add(new DropDownChoice<String>("teamId", teams).setRequired(true));

        add(new FeedbackPanel("feedback"));
    }

}
