/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui;

import java.io.File;
import java.io.IOException;

import logbook.config.GlobalConfig;
import logbook.config.ShipConfig;
import logbook.gui.background.AsyncExecApplicationMain;
import logbook.gui.background.AsyncExecApplicationMainConsole;
import logbook.gui.background.AsyncExecUpdateCheck;
import logbook.gui.listener.BathwaterTableAdapter;
import logbook.gui.listener.CalcExpAdapter;
import logbook.gui.listener.ConfigDialogAdapter;
import logbook.gui.listener.CreateItemReportAdapter;
import logbook.gui.listener.CreateShipReportAdapter;
import logbook.gui.listener.DropReportAdapter;
import logbook.gui.listener.HelpEventListener;
import logbook.gui.listener.ItemListReportAdapter;
import logbook.gui.listener.MainShellAdapter;
import logbook.gui.listener.ShipListReportAdapter;
import logbook.gui.listener.TraySelectionListener;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.Sound;
import logbook.server.proxy.ProxyServer;
import logbook.thread.ThreadManager;
import logbook.thread.ThreadStateObserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * メイン画面
 *
 */
public final class ApplicationMain {

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ApplicationMain.class);

    /** シェル */
    private Shell shell;

    /** トレイ */
    private TrayItem trayItem;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {
        try {
            // 設定読み込み
            ShipConfig.load();
            // アプリケーション開始
            ApplicationMain window = new ApplicationMain();
            window.open();
            // ウインドウが閉じたタイミングで設定を書き込みます
            GlobalConfig.store();
            ShipConfig.store();
        } catch (Error e) {
            LOG.fatal("メインスレッドが異常終了しました", e);
        } catch (Exception e) {
            LOG.fatal("メインスレッドが異常終了しました", e);
        } finally {
            // リソースを開放する
            SWTResourceManager.dispose();
            // プロキシサーバーをシャットダウンする
            ProxyServer.end();
            // 報告書を保存する
            try {
                CreateReportLogic.writeCsvStripFirstColumn(new File("./海戦・ドロップ報告書.csv"),
                        CreateReportLogic.getBattleResultStoreHeader(),
                        CreateReportLogic.getBattleResultStoreBody(), true);
                CreateReportLogic.writeCsvStripFirstColumn(new File("./建造報告書.csv"),
                        CreateReportLogic.getCreateShipHeader(),
                        CreateReportLogic.getCreateShipBody(), true);
                CreateReportLogic.writeCsvStripFirstColumn(new File("./開発報告書.csv"),
                        CreateReportLogic.getCreateItemHeader(),
                        CreateReportLogic.getCreateItemBody(), true);
            } catch (IOException e) {
                LOG.warn("報告書の保存に失敗しました", e);
            }
        }
    }

    /**
     * Open the window.
     */
    public void open() {
        Display display = Display.getDefault();
        this.createContents();
        this.shell.open();
        this.shell.layout();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        this.trayItem.dispose();
    }

    /**
     * 画面レイアウトを作成します
     */
    public void createContents() {

        final Display display = Display.getDefault();
        this.shell = new Shell(SWT.SHELL_TRIM | GlobalConfig.getOnTop());
        this.shell.setText("航海日誌 " + GlobalConfig.VERSION);
        GridLayout glShell = new GridLayout(1, false);
        glShell.horizontalSpacing = 1;
        glShell.marginTop = 0;
        glShell.marginWidth = 0;
        glShell.marginHeight = 0;
        glShell.marginBottom = 0;
        glShell.verticalSpacing = 1;
        this.shell.setLayout(glShell);

        // メニューバー
        Menu menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(menubar);
        MenuItem cmdmenuroot = new MenuItem(menubar, SWT.CASCADE);
        cmdmenuroot.setText("コマンド");
        Menu cmdmenu = new Menu(cmdmenuroot);
        cmdmenuroot.setMenu(cmdmenu);
        MenuItem calcmenuroot = new MenuItem(menubar, SWT.CASCADE);
        calcmenuroot.setText("計算機");
        Menu calcmenu = new Menu(calcmenuroot);
        calcmenuroot.setMenu(calcmenu);
        MenuItem etcroot = new MenuItem(menubar, SWT.CASCADE);
        etcroot.setText("その他");
        Menu etcmenu = new Menu(etcroot);
        etcroot.setMenu(etcmenu);

        // メニュー
        // コマンド-ドロップ報告書
        MenuItem cmddrop = new MenuItem(cmdmenu, SWT.NONE);
        cmddrop.setText("ドロップ報告書(&D)\tCtrl+D");
        cmddrop.setAccelerator(SWT.CTRL + 'D');
        cmddrop.addSelectionListener(new DropReportAdapter(this.shell));
        // コマンド-建造報告書
        MenuItem cmdcreateship = new MenuItem(cmdmenu, SWT.NONE);
        cmdcreateship.setText("建造報告書(&B)\tCtrl+B");
        cmdcreateship.setAccelerator(SWT.CTRL + 'B');
        cmdcreateship.addSelectionListener(new CreateShipReportAdapter(this.shell));
        // コマンド-開発報告書
        MenuItem cmdcreateitem = new MenuItem(cmdmenu, SWT.NONE);
        cmdcreateitem.setText("開発報告書(&E)\tCtrl+E");
        cmdcreateitem.setAccelerator(SWT.CTRL + 'E');
        cmdcreateitem.addSelectionListener(new CreateItemReportAdapter(this.shell));
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-所有装備一覧
        MenuItem cmditemlist = new MenuItem(cmdmenu, SWT.NONE);
        cmditemlist.setText("所有装備一覧(&I)\tCtrl+I");
        cmditemlist.setAccelerator(SWT.CTRL + 'I');
        cmditemlist.addSelectionListener(new ItemListReportAdapter(this.shell));
        // コマンド-所有艦娘一覧
        MenuItem cmdshiplist = new MenuItem(cmdmenu, SWT.NONE);
        cmdshiplist.setText("所有艦娘一覧(&S)\tCtrl+S");
        cmdshiplist.setAccelerator(SWT.CTRL + 'S');
        cmdshiplist.addSelectionListener(new ShipListReportAdapter(this.shell));
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-お風呂に入りたい艦娘
        MenuItem cmdbathwaterlist = new MenuItem(cmdmenu, SWT.NONE);
        cmdbathwaterlist.setText("お風呂に入りたい艦娘(&N)\tCtrl+N");
        cmdbathwaterlist.setAccelerator(SWT.CTRL + 'N');
        cmdbathwaterlist.addSelectionListener(new BathwaterTableAdapter(this.shell));
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // 表示-縮小表示
        final MenuItem dispsize = new MenuItem(cmdmenu, SWT.CHECK);
        dispsize.setText("縮小表示(&M)\tCtrl+M");
        dispsize.setAccelerator(SWT.CTRL + 'M');

        // 計算機-経験値計算
        MenuItem calcexp = new MenuItem(calcmenu, SWT.NONE);
        calcexp.setText("経験値計算機(&C)\tCtrl+C");
        calcexp.setAccelerator(SWT.CTRL + 'C');
        calcexp.addSelectionListener(new CalcExpAdapter(this.shell));

        // ヘルプ-設定
        MenuItem config = new MenuItem(etcmenu, SWT.NONE);
        config.setText("設定(&P)");
        config.addSelectionListener(new ConfigDialogAdapter(this.shell));
        // ヘルプ-自動プロキシ構成スクリプトファイル生成
        MenuItem pack = new MenuItem(etcmenu, SWT.NONE);
        pack.setText("自動プロキシ構成スクリプト");
        pack.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new CreatePacFileDialog(ApplicationMain.this.shell).open();
            }
        });
        // ヘルプ-バージョン情報
        MenuItem version = new MenuItem(etcmenu, SWT.NONE);
        version.setText("バージョン情報(&A)");
        version.addSelectionListener(new HelpEventListener(this.shell));

        // シェルイベント
        this.shell.addShellListener(new MainShellAdapter());
        // キーが押された時に呼ばれるリスナーを追加します
        this.shell.addHelpListener(new HelpEventListener(this.shell));

        this.trayItem = this.addTrayItem(display);

        // コマンドボタン
        final Composite command = new Composite(this.shell, SWT.NONE);
        RowLayout rlCommand = new RowLayout();
        rlCommand.spacing = 1;
        command.setLayout(rlCommand);
        command.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button dropResult = new Button(command, SWT.PUSH);
        dropResult.setText("ドロップ報告書");
        dropResult.addSelectionListener(new DropReportAdapter(this.shell));
        Button createShip = new Button(command, SWT.PUSH);
        createShip.setText("建造報告書");
        createShip.addSelectionListener(new CreateShipReportAdapter(this.shell));
        Button createItem = new Button(command, SWT.PUSH);
        createItem.setText("開発報告書");
        createItem.addSelectionListener(new CreateItemReportAdapter(this.shell));
        Button itemList = new Button(command, SWT.PUSH);
        itemList.setText("所有装備一覧(0)");
        itemList.addSelectionListener(new ItemListReportAdapter(this.shell));
        Button shipList = new Button(command, SWT.PUSH);
        shipList.setText("所有艦娘一覧(0)");
        shipList.addSelectionListener(new ShipListReportAdapter(this.shell));

        // 遠征
        Group deckGroup = new Group(this.shell, SWT.NONE);
        deckGroup.setText("遠征");
        deckGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glDeckGroup = new GridLayout(2, false);
        glDeckGroup.verticalSpacing = 1;
        glDeckGroup.marginTop = 0;
        glDeckGroup.marginWidth = 0;
        glDeckGroup.marginHeight = 0;
        glDeckGroup.marginBottom = 0;
        glDeckGroup.horizontalSpacing = 1;
        deckGroup.setLayout(glDeckGroup);

        final Button deckNotice = new Button(deckGroup, SWT.CHECK);
        deckNotice.setSelection(GlobalConfig.getNoticeDeckmission());
        deckNotice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        deckNotice.setText("1分前に通知する");
        deckNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                GlobalConfig.setNoticeDeckmission(deckNotice.getSelection());
            }
        });

        final Label deck1name = new Label(deckGroup, SWT.NONE);
        deck1name.setText("ここに艦隊2の艦隊名が入ります");
        deck1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text deck1time = new Text(deckGroup, SWT.SINGLE | SWT.BORDER);
        deck1time.setText("艦隊2の帰投時間");
        deck1time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData gddeck1time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck1time.widthHint = 75;
        deck1time.setLayoutData(gddeck1time);

        final Label deck2name = new Label(deckGroup, SWT.NONE);
        deck2name.setText("ここに艦隊3の艦隊名が入ります");
        deck2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text deck2time = new Text(deckGroup, SWT.SINGLE | SWT.BORDER);
        deck2time.setText("艦隊3の帰投時間");
        deck2time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData gddeck2time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck2time.widthHint = 75;
        deck2time.setLayoutData(gddeck2time);

        final Label deck3name = new Label(deckGroup, SWT.NONE);
        deck3name.setText("ここに艦隊4の艦隊名が入ります");
        deck3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text deck3time = new Text(deckGroup, SWT.SINGLE | SWT.BORDER);
        deck3time.setText("艦隊4の帰投時間");
        deck3time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData gddeck3time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck3time.widthHint = 75;
        deck3time.setLayoutData(gddeck3time);

        // 入渠
        Group ndockGroup = new Group(this.shell, SWT.NONE);
        ndockGroup.setText("入渠");
        ndockGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glNdockGroup = new GridLayout(2, false);
        glNdockGroup.verticalSpacing = 1;
        glNdockGroup.marginTop = 0;
        glNdockGroup.marginWidth = 0;
        glNdockGroup.marginHeight = 0;
        glNdockGroup.marginBottom = 0;
        glNdockGroup.horizontalSpacing = 1;
        ndockGroup.setLayout(glNdockGroup);

        final Button ndockNotice = new Button(ndockGroup, SWT.CHECK);
        ndockNotice.setSelection(GlobalConfig.getNoticeNdock());
        ndockNotice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        ndockNotice.setText("1分前に通知する");
        ndockNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                GlobalConfig.setNoticeNdock(ndockNotice.getSelection());
            }
        });

        final Label ndock1name = new Label(ndockGroup, SWT.NONE);
        ndock1name.setText("ドッグ1に浸かっている艦娘の名前");
        ndock1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock1time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock1time.setText("お風呂から上がる時間");
        ndock1time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData gdndock1time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock1time.widthHint = 75;
        ndock1time.setLayoutData(gdndock1time);

        final Label ndock2name = new Label(ndockGroup, SWT.NONE);
        ndock2name.setText("ドッグ2に浸かっている艦娘の名前");
        ndock2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock2time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock2time.setText("お風呂から上がる時間");
        ndock2time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData gdndock2time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock2time.widthHint = 75;
        ndock2time.setLayoutData(gdndock2time);

        final Label ndock3name = new Label(ndockGroup, SWT.NONE);
        ndock3name.setText("ドッグ3に浸かっている艦娘の名前");
        ndock3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock3time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock3time.setText("お風呂から上がる時間");
        ndock3time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData gdndock3time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock3time.widthHint = 75;
        ndock3time.setLayoutData(gdndock3time);

        final Label ndock4name = new Label(ndockGroup, SWT.NONE);
        ndock4name.setText("ドッグ4に浸かっている艦娘の名前");
        ndock4name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock4time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock4time.setText("お風呂から上がる時間");
        ndock4time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData gdndock4time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock4time.widthHint = 75;
        ndock4time.setLayoutData(gdndock4time);

        // コンソール
        final Composite consolec = new Composite(this.shell, SWT.NONE);
        GridLayout loglayout = new GridLayout(1, false);
        loglayout.verticalSpacing = 1;
        loglayout.marginTop = 0;
        loglayout.marginWidth = 0;
        loglayout.marginHeight = 0;
        loglayout.marginBottom = 0;
        loglayout.horizontalSpacing = 1;
        consolec.setLayout(loglayout);
        consolec.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        org.eclipse.swt.widgets.List console = new org.eclipse.swt.widgets.List(consolec, SWT.BORDER | SWT.V_SCROLL);
        console.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        // 初期設定 縮小表示が有効なら縮小表示にする
        if (GlobalConfig.getMinimumLayout()) {
            this.shell.setRedraw(false);
            ApplicationMain.this.hide(true, command, deckNotice, deck1name, deck2name, deck3name, ndockNotice,
                    ndock1name, ndock2name, ndock3name, ndock4name, consolec);
            dispsize.setSelection(true);
            this.shell.pack();
            this.shell.setRedraw(true);
        } else {
            this.shell.setSize(GlobalConfig.getWidth(), GlobalConfig.getHeight());
        }

        // 縮小表示チェック時の動作
        dispsize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ApplicationMain.this.shell.setRedraw(false);
                boolean minimum = dispsize.getSelection();
                // コントロールを隠す
                ApplicationMain.this.hide(minimum, command, deckNotice, deck1name, deck2name, deck3name, ndockNotice,
                        ndock1name, ndock2name, ndock3name, ndock4name, consolec);
                ApplicationMain.this.shell.setRedraw(true);
                // ウインドウサイズを調節
                if (minimum) {
                    ApplicationMain.this.shell.pack();
                } else {
                    ApplicationMain.this.shell.setSize(GlobalConfig.getWidth(), GlobalConfig.getHeight());
                }
                // 設定を保存
                GlobalConfig.setMinimumLayout(minimum);
            }
        });

        // プロキシサーバーを開始する
        ThreadManager.regist(ProxyServer.getInstance());

        // 非同期で画面を更新するスレッド
        ThreadManager.regist(new AsyncExecApplicationMain(this.shell, this.trayItem, itemList, shipList,
                deckNotice, deck1name, deck1time, deck2name, deck2time, deck3name, deck3time, ndockNotice, ndock1name,
                ndock1time, ndock2name, ndock2time, ndock3name, ndock3time, ndock4name, ndock4time));
        // 非同期でログを出すスレッド
        ThreadManager.regist(new AsyncExecApplicationMainConsole(console));
        // サウンドを出すスレッド
        ThreadManager.regist(new Sound.PlayerThread());
        // スレッドを監視するスレッド
        ThreadManager.regist(new ThreadStateObserver(this.shell));

        ThreadManager.start();

        // アップデートチェックする
        if (GlobalConfig.getCheckUpdate()) {
            new AsyncExecUpdateCheck(this.shell).start();
        }
    }

    /**
     * トレイアイコンを追加します
     * 
     * @param display
     * @return
     */
    private TrayItem addTrayItem(final Display display) {
        // トレイアイコンを追加します
        Tray tray = display.getSystemTray();
        TrayItem item = new TrayItem(tray, SWT.NONE);
        Image image = display.getSystemImage(SWT.ICON_INFORMATION);
        item.setImage(image);
        item.addListener(SWT.Selection, new TraySelectionListener(this.shell));
        return item;
    }

    /**
     * 縮小表示と通常表示とを切り替えます
     * 
     * @param minimum
     * @param controls 隠すコントロール
     */
    private void hide(boolean minimum, Control... controls) {
        for (Control control : controls) {
            LayoutLogic.hide(control, minimum);
        }
        this.shell.layout();
    }
}