package seq.gui;

import java.io.Serializable;
import seq.entities.GameModel;

/**
 *
 */
public abstract class BaseController
{
    protected BaseView view;
    protected GameModel model;

    public abstract void launch();
    public abstract void restart(boolean repeatGame);
    public abstract void destroy();
}
