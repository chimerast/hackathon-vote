package org.e2d3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.rating.RatingPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {

    private User user;
    private Map<String, Rating> ratings;
    private List<User> users;

    public HomePage(PageParameters parameters) {
        super(parameters);

        if (!UserData.get().isAuthenticated()) {
            throw new RestartResponseException(LoginPage.class);
        }

        this.user = DAO.getUser(UserData.get().getUserId());
        this.ratings = new HashMap<>();
        this.users = DAO.getUserList();

        if (this.user.ratings != null) {
            this.user.ratings.forEach((rating) -> ratings.put(rating.teamId, rating));
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new BookmarkablePageLink<Void>("edit", EditTeamPage.class));
        add(new ExternalLink("view", StaticContents.getUrl(user.teamId)));

        add(new Link<Void>("logout") {
            @Override
            public void onClick() {
                UserData.get().setUserId(null);
                setResponsePage(HomePage.class);
            }
        });

        add(new ListView<Team>("teams", new LoadableDetachableModel<List<Team>>() {
            @Override
            protected List<Team> load() {
                /*

                Random rand = new Random(user.id.hashCode());
                List<Team> teams = DAO.getTeamList();
                int size = teams.size();

                boolean first = (user.teamId.charAt(0) - 'A') < (size / 2);
                teams = teams.stream().filter((t) -> {
                    boolean pos = (t.id.charAt(0) - 'A') < (size / 2);
                    return first != pos;
                }).collect(Collectors.toList());
                
                Collections.shuffle(teams, rand);
                return teams;
                */

                return DAO
                    .getTeamList()
                    .stream()
                    .filter((t) -> !t.id.equals(user.teamId))
                    .sorted(Comparator.comparing((t) -> t.id))
                    .collect(Collectors.toList());
            }
        }) {
            @Override
            protected void populateItem(ListItem<Team> item) {
                item.add(new Label("id"));
                item.add(new Label("comment"));

                ExternalLink url = new ExternalLink("url", StaticContents.getUrl(item.getModelObject()));
                url.add(new Label("title"));

                item.add(url);

                Rating rating = ratings.get(item.getModelObject().id);
                if (rating == null) {
                    rating = new Rating();
                    rating.teamId = item.getModelObject().id;
                }

                item.add(new StarPanel("action", rating.teamId, new PropertyModel<Integer>(rating, "action")));
                item.add(new StarPanel("visual", rating.teamId, new PropertyModel<Integer>(rating, "visual")));
                item.add(new StarPanel("story", rating.teamId, new PropertyModel<Integer>(rating, "story")));

                String teamId = rating.teamId;

                String members = users.stream().filter((u) -> u.teamId.equals(teamId)).map((u) -> u.id).collect(Collectors.joining(" "));

                item.add(new Label("members", members));
            }

            @Override
            protected IModel<Team> getListItemModel(IModel<? extends List<Team>> listViewModel, int index) {
                return new CompoundPropertyModel<Team>(super.getListItemModel(listViewModel, index));
            }
        });
    }

    /**
     * ランダム
     */
    public class StarPanel extends RatingPanel {

        private String teamId;

        public StarPanel(String id, String teamId, IModel<? extends Number> rating) {
            super(id, rating, 5, false);

            this.teamId = teamId;
        }

        @Override
        protected void onRated(int rating, AjaxRequestTarget target) {
            setDefaultModelObject(rating);

            if (user.ratings == null) {
                user.ratings = new ArrayList<Rating>();
            }

            Optional<Rating> optional = user.ratings.stream().filter((r) -> r.teamId == teamId).findFirst();
            Rating rt;
            if (optional.isPresent()) {
                rt = optional.get();
            } else {
                rt = new Rating();
                rt.teamId = teamId;
                user.ratings.add(rt);
            }

            switch (getId()) {
            case "action":
                rt.action = rating;
                break;
            case "visual":
                rt.visual = rating;
                break;
            case "story":
                rt.story = rating;
                break;
            }

            DAO.save(user);
        }

        @Override
        protected boolean onIsStarActive(int star) {
            return star < (Integer) getDefaultModelObject();
        }
    }

}
