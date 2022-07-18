package io.github.sergkhram.ui.views.list;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.repository.HostRepository;
import io.github.sergkhram.ui.views.list.forms.HostForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.sergkhram.ui.generators.Generator.generateRandomString;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class HostsListViewTest {

    @Autowired
    private HostsListView hostsListView;

    @Autowired
    HostRepository hostRepository;

    @BeforeEach
    public void beforeTest() {
        hostsListView.grid.setItems(List.of());
        hostsListView.filterText.clear();
        hostRepository.deleteAll();
    }

    @Test
    public void checkFormShownWhenContactSelected() {
        Grid<Host> grid = hostsListView.grid;
        Host host = new Host();
        host.setName(generateRandomString());
        host.setAddress(generateRandomString());
        grid.setItems(List.of(host));
        Host firstContact = getFirstItem(grid);
        HostForm form = hostsListView.form;
        Assertions.assertFalse(form.isVisible());
        grid.asSingleSelect().setValue(firstContact);
        Assertions.assertTrue(form.isVisible());
        Assertions.assertEquals(firstContact.getName(), form.name.getValue());
    }

    @Test
    public void checkGridSizeMustBeCorrected() {
        Grid<Host> grid = hostsListView.grid;
        int count = 100;
        grid.setPageSize(count);
        List<Host> hosts = generateHosts(count);
        Assertions.assertEquals(0, getGridAsList(grid).getItems().size());
        grid.setItems(hosts);
        Assertions.assertEquals(count, getGridAsList(grid).getItems().size());
        Assertions.assertEquals(count, grid.getPageSize());
    }

    @Test
    public void checkFilterTextUpdateValue() {
        TextField filterText = hostsListView.filterText;
        String value = generateRandomString();
        Assertions.assertEquals("", filterText.getValue());
        filterText.setValue(value);
        Assertions.assertEquals(value, filterText.getValue());
        filterText.clear();
        Assertions.assertEquals("", filterText.getValue());
    }

    @Test
    public void checkAddHostButtonAction() {
        Button addHostButton = hostsListView.addHostButton;
        HostForm form = hostsListView.form;
        Assertions.assertFalse(form.isVisible());
        addHostButton.click();
        Assertions.assertTrue(form.isVisible());
    }

    @Test
    public void checkFilterGridElements() {
        TextField filterText = hostsListView.filterText;
        Host firstHost = new Host("name", "name", 1);
        Host secondHost = new Host("qwe", "qwe", 1);
        Host thirdHost = new Host("zxc", "zxc", 1);
        hostRepository.saveAll(List.of(firstHost, secondHost, thirdHost));
        Grid<Host> grid = hostsListView.grid;
        grid.setItems(hostRepository.findAll());
        Assertions.assertEquals(3, getGridAsList(grid).getItems().size());
        filterText.setValue(firstHost.getName());
        Assertions.assertEquals(1, getGridAsList(grid).getItems().size());
        Assertions.assertEquals(firstHost.getName(), getGridAsList(grid).getItems().stream().findFirst().get().getName());
        filterText.setValue(secondHost.getName());
        Assertions.assertEquals(1, getGridAsList(grid).getItems().size());
        Assertions.assertEquals(secondHost.getName(), getGridAsList(grid).getItems().stream().findFirst().get().getName());
        filterText.setValue(thirdHost.getName());
        Assertions.assertEquals(1, getGridAsList(grid).getItems().size());
        Assertions.assertEquals(thirdHost.getName(), getGridAsList(grid).getItems().stream().findFirst().get().getName());
        filterText.setValue("e");
        Assertions.assertEquals(2, getGridAsList(grid).getItems().size());
        Assertions.assertTrue(
            getGridAsList(grid).getItems()
                .stream()
                .map(Host::getName)
                .collect(
                    Collectors.toList()
                ).containsAll(
                    List.of(firstHost.getName(), secondHost.getName())
            )
        );
    }

    private ListDataProvider<Host> getGridAsList(Grid<Host> grid) {
        return ((ListDataProvider<Host>) grid.getDataProvider());
    }

    private Host getFirstItem(Grid<Host> grid) {
        return getGridAsList(grid).getItems().iterator().next();
    }

    private List<Host> generateHosts(int count) {
        CopyOnWriteArrayList<Host> hosts = new CopyOnWriteArrayList<>();
        IntStream.range(0, count).parallel().forEach(
            it -> {
                Host host = new Host();
                host.setName(generateRandomString());
                host.setAddress(generateRandomString());
                hosts.add(
                    host
                );
            }
        );
        return hosts;
    }
}