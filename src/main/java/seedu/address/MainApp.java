package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Version;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.ConfigUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.AssignTaskCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyTaskMasterPro;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonTaskMasterProStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.TaskMasterProStorage;
import seedu.address.storage.UserPrefsStorage;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(0, 2, 2, true);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing TaskMasterPro ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());
        initLogging(config);

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        TaskMasterProStorage taskMasterProStorage = new JsonTaskMasterProStorage(userPrefs.getTaskMasterProFilePath());
        storage = new StorageManager(taskMasterProStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s TaskMasterPro and {@code userPrefs}. <br>
     * The data from the sample TaskMasterPro will be used instead if {@code storage}'s TaskMasterPro is not found,
     * or an empty TaskMasterPro will be used instead if errors occur when reading {@code storage}'s TaskMasterPro.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getTaskMasterProFilePath());

        Optional<ReadOnlyTaskMasterPro> taskMasterProOptional;
        ReadOnlyTaskMasterPro initialData;
        boolean isSampleData = false;
        try {
            taskMasterProOptional = storage.readTaskMasterPro();
            if (!taskMasterProOptional.isPresent()) {
                logger.info("Creating a new data file " + storage.getTaskMasterProFilePath()
                        + " populated with a sample TaskMasterPro.");
                isSampleData = true;
            }
            initialData = taskMasterProOptional.orElseGet(SampleDataUtil::getSampleTaskMasterPro);
        } catch (Exception e) {
            logger.warning("Data file at " + storage.getTaskMasterProFilePath() + " could not be loaded."
                    + " Will be starting with a default TaskMasterPro.");
            initialData = SampleDataUtil.getSampleTaskMasterPro();
            isSampleData = true;
        }

        Model model = new ModelManager(initialData, userPrefs);

        if (isSampleData) {
            try {
                AssignTaskCommand atc = new AssignTaskCommand(1, 1);
                atc.execute(model);
                atc = new AssignTaskCommand(1, 2);
                atc.execute(model);
                atc = new AssignTaskCommand(1, 3);
                atc.execute(model);
                atc = new AssignTaskCommand(2, 3);
                atc.execute(model);
                atc = new AssignTaskCommand(2, 4);
                atc.execute(model);
                atc = new AssignTaskCommand(2, 5);
                atc.execute(model);
                atc = new AssignTaskCommand(3, 5);
                atc.execute(model);
                atc = new AssignTaskCommand(3, 6);
                atc.execute(model);
            } catch (Exception e) {
                logger.warning("Error with generating sample data.");
            }
        }

        return model;
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     *
     * @param configFilePath The path to the config file.
     * @return The {@code Config} using the file at {@code configFilePath}.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            if (!configOptional.isPresent()) {
                logger.info("Creating new config file " + configFilePathUsed);
            }
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataLoadingException e) {
            logger.warning("Config file at " + configFilePathUsed + " could not be loaded."
                    + " Using default config properties.");
            initializedConfig = new Config();
        }

        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     *
     * @param storage The file with user preference.
     * @return The representation of user preference.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (!prefsOptional.isPresent()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting TaskMasterPro " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping TaskMasterPro ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
