package seq.gui;

import java.io.Serializable;
import javax.swing.JFrame;

/**
 * Base class for a view.
 */
public abstract class BaseView implements Serializable
{
    protected JFrame mainComponent;
    transient protected BaseController controller;

    public BaseView()
    {}

    public BaseView(JFrame mainComponent, BaseController controller)
    {
        this.mainComponent = mainComponent;
        this.controller = controller;
    }

    public abstract void init();
    public abstract void restart();
    public abstract void release();
}
