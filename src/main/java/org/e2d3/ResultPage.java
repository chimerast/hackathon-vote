package org.e2d3;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ResultPage extends WebPage {

    private List<User> users;
    private String sort;

    public ResultPage(PageParameters parameters) {
        super(parameters);

        this.users = DAO.getUserList();
        this.sort = parameters.get("sort").toString("id");
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new ListView<Team>("teams", new LoadableDetachableModel<List<Team>>() {
            @Override
            protected List<Team> load() {
                List<Team> teams = DAO.getTeamList();
                switch (sort) {
                case "action":
                    Collections.sort(teams, Comparator.comparingDouble((t) -> -average(t.id, (r) -> r.action)));
                    break;
                case "visual":
                    Collections.sort(teams, Comparator.comparingDouble((t) -> -average(t.id, (r) -> r.visual)));
                    break;
                case "story":
                    Collections.sort(teams, Comparator.comparingDouble((t) -> -average(t.id, (r) -> r.story)));
                    break;
                case "total":
                    Collections.sort(teams, Comparator.comparingDouble((t) -> -total(t.id)));
                    break;
                default:
                    Collections.sort(teams, Comparator.comparing((t) -> t.id));
                    break;
                }
                return teams;
            }
        }) {
            @Override
            protected void populateItem(ListItem<Team> item) {
                String teamId = item.getModelObject().id;

                item.add(new Label("id"));

                ExternalLink url = new ExternalLink("url", StaticContents.getUrl(item.getModelObject()));
                url.add(new Label("title"));

                item.add(url);

                String members = users.stream().filter((u) -> u.teamId.equals(teamId)).map((u) -> u.id).collect(Collectors.joining(" "));
                item.add(new Label("members", members));

                Double action = average(teamId, (r) -> r.action);
                Double visual = average(teamId, (r) -> r.visual);
                Double story = average(teamId, (r) -> r.story);
                Double total = total(teamId);

                item.add(new Label("action", String.format("%.2f", action)));
                item.add(new Label("visual", String.format("%.2f", visual)));
                item.add(new Label("story", String.format("%.2f", story)));
                item.add(new Label("total", String.format("%.2f", total)));
            }

            @Override
            protected IModel<Team> getListItemModel(IModel<? extends List<Team>> listViewModel, int index) {
                return new CompoundPropertyModel<Team>(super.getListItemModel(listViewModel, index));
            }
        });
    }

    private Double average(String teamId, Function<Rating, Integer> extractor) {
        return users
            .stream()
            .filter((u) -> u.ratings != null)
            .map((u) -> u.ratings.stream().filter((t) -> t.teamId.equals(teamId)).findFirst())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(extractor)
            .filter((r) -> r != 0)
            .collect(Collectors.averagingDouble((r) -> r));
    }

    private Double total(String teamId) {
        return users
            .stream()
            .map((u) -> u.ratings.stream().filter((t) -> t.teamId.equals(teamId)).findFirst())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter((r) -> r.action != 0 && r.visual != 0 && r.story != 0)
            .map((r) -> r.action + r.visual + r.story)
            .collect(Collectors.averagingDouble((r) -> r));
    }
}
