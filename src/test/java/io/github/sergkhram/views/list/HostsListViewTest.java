package io.github.sergkhram.views.list;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.views.list.forms.HostForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class HostsListViewTest {

    @Autowired
    private HostsListView hostsListView;

    @Test
    public void formShownWhenContactSelected() {
        Grid<Host> grid = hostsListView.grid;
        Host firstContact = getFirstItem(grid);

        HostForm form = hostsListView.form;

        Assertions.assertFalse(form.isVisible());
        grid.asSingleSelect().setValue(firstContact);
        Assertions.assertTrue(form.isVisible());
        Assertions.assertEquals(firstContact.getName(), form.name.getValue());
    }

    private Host getFirstItem(Grid<Host> grid) {
        return( (ListDataProvider<Host>) grid.getDataProvider()).getItems().iterator().next();
    }
}