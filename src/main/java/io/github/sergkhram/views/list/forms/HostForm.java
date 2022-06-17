package io.github.sergkhram.views.list.forms;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import io.github.sergkhram.data.entity.Host;

public class HostForm extends FormLayout {
  public TextField name = new TextField("Host name");
  TextField address = new TextField("Address");
  IntegerField port = new IntegerField("Port");

  Button save = new Button("Save");
  Button delete = new Button("Delete");
  Button close = new Button("Cancel");

  Binder<Host> binder = new BeanValidationBinder<>(Host.class);
  private Host host;

  public HostForm() {
    addClassName("host-form");
    binder.bindInstanceFields(this);

    add(name,
        address,
        port,
        createButtonsLayout());
  }

  private HorizontalLayout createButtonsLayout() {
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); 
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    save.addClickShortcut(Key.ENTER); 
    close.addClickShortcut(Key.ESCAPE);

    save.addClickListener(event -> validateAndSave());
    delete.addClickListener(event -> fireEvent(new DeleteEvent(this, host)));
    close.addClickListener(event -> fireEvent(new CloseEvent(this)));

    binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

    return new HorizontalLayout(save, delete, close); 
  }

  private void validateAndSave() {
    try {
      //TODO try to connect and change the state of host if successful
      binder.writeBean(host);
      fireEvent(new SaveEvent(this, host));
    } catch (ValidationException e) {
      e.printStackTrace();
    }
  }

  public void setHost(Host host) {
    this.host = host;
    binder.readBean(host);
  }

  public static abstract class HostFormEvent extends ComponentEvent<HostForm> {
    private Host host;

    protected HostFormEvent(HostForm source, Host host) {
      super(source, false);
      this.host = host;
    }

    public Host getHost() {
      return host;
    }
  }

  public static class SaveEvent extends HostFormEvent {
    SaveEvent(HostForm source, Host host) {
      super(source, host);
      Notification.show(
          "Host with name " + host.getAddress() + " is saved",
          5000,
          Notification.Position.TOP_CENTER
      );
    }
  }

  public static class DeleteEvent extends HostFormEvent {
    DeleteEvent(HostForm source, Host host) {
      super(source, host);
    }
  }

  public static class CloseEvent extends HostFormEvent {
    CloseEvent(HostForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}