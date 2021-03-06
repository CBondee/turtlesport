package fr.turtlesport.ui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXHyperlink;

import fr.turtlesport.lang.ILanguage;
import fr.turtlesport.lang.LanguageEvent;
import fr.turtlesport.lang.LanguageListener;
import fr.turtlesport.lang.LanguageManager;
import fr.turtlesport.map.AllMapsFactory;
import fr.turtlesport.ui.swing.component.PanelPrefListener;
import fr.turtlesport.util.BrowserUtil;
import fr.turtlesport.util.ResourceBundleUtility;

/**
 * @author Denis Apparicio
 * 
 */
public class JPanelPrefMap extends JPanel implements LanguageListener,
                                         PanelPrefListener {

  private JPanelPrefTitle jPanelTitle;

  private JPanel          jPanelCenter;

  private JLabel          jLabelLibCacheSize;

  private JLabel          jLabelValCacheSize;

  private JButton         jButtonDelete;

  private JLabel          jLabelOpenStreeetMap;

  private ResourceBundle  rb;

  private JPanel          jPanelSouth;

  private JXHyperlink     jxHyperlinkOpenStreetMap;

  private JXHyperlink     jxHyperlinklicence;

  private JLabel          jLabelLicence;

  /**
   * 
   */
  public JPanelPrefMap() {
    super();
    initialize();
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.turtlesport.ui.swing.component.PanelPrefListener#viewChanged()
   */
  public void viewChanged() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * fr.turtlesport.lang.LanguageListener#languageChanged(fr.turtlesport.lang
   * .LanguageEvent)
   */
  public void languageChanged(final LanguageEvent event) {
    if (SwingUtilities.isEventDispatchThread()) {
      performedLanguage(event.getLang());
    }
    else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          performedLanguage(event.getLang());
        }
      });
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.turtlesport.lang.LanguageListener#completedRemoveLanguageListener()
   */
  public void completedRemoveLanguageListener() {
  }

  private void performedLanguage(ILanguage lang) {
    rb = ResourceBundleUtility.getBundle(lang, getClass());

    jPanelTitle.setTitle(rb.getString("title"));
    jLabelLibCacheSize.setText(rb.getString("jLabelLibCacheSize"));
    jButtonDelete.setText(rb.getString("jButtonDelete"));
    jLabelLicence.setText(rb.getString("licence"));
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    BorderLayout borderLayout = new BorderLayout();
    borderLayout.setHgap(5);
    borderLayout.setVgap(5);
    this.setLayout(borderLayout);
    this.add(getJPanelTitle(), BorderLayout.NORTH);
    this.add(getJPanelCenter(), BorderLayout.CENTER);
    this.add(getJPanelSouth(), BorderLayout.SOUTH);

    // value
    // Efface le cache disque
    jLabelValCacheSize.setText(cacheSize());

    // Evenement
    jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        MainGui.getWindow().beforeRunnableSwing();

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            // Efface le cache disque
            AllMapsFactory.getInstance().cleanCache();
            jLabelValCacheSize.setText(cacheSize());
            MainGui.getWindow().afterRunnableSwing();
          }
        });

      }
    });

    performedLanguage(LanguageManager.getManager().getCurrentLang());
    LanguageManager.getManager().addLanguageListener(this);
  }

  /**
   * This method initializes jPanelTitle
   * 
   * @return javax.swing.JPanel
   */
  private JPanelPrefTitle getJPanelTitle() {
    if (jPanelTitle == null) {
      jPanelTitle = new JPanelPrefTitle("Map");
    }
    return jPanelTitle;
  }

  /**
   * This method initializes jPanelCenter
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanelCenter() {
    if (jPanelCenter == null) {
      jLabelLibCacheSize = new JLabel();
      jLabelLibCacheSize.setFont(GuiFont.FONT_PLAIN);
      jLabelValCacheSize = new JLabel();
      jLabelValCacheSize.setFont(GuiFont.FONT_PLAIN);
      jLabelLibCacheSize.setLabelFor(jLabelLibCacheSize);

      jPanelCenter = new JPanel();
      FlowLayout flowLayout = new FlowLayout();
      flowLayout.setAlignment(FlowLayout.LEFT);
      jPanelCenter.setLayout(flowLayout);
      jPanelCenter.setAlignmentY(TOP_ALIGNMENT);
      jPanelCenter.add(jLabelLibCacheSize, null);
      jPanelCenter.add(jLabelValCacheSize, null);
      jPanelCenter.add(getJButtonDelete(), null);
    }
    return jPanelCenter;
  }

  /**
   * This method initializes jPanelCenter
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanelSouth() {
    if (jPanelSouth == null) {
      jLabelOpenStreeetMap = new JLabel();
      jLabelOpenStreeetMap.setFont(GuiFont.FONT_PLAIN);
      // jLabelOpenStreeetMap
      // .setText("<html><body>Data/Maps Copyright &#169; 2012</body></html>");
      jLabelOpenStreeetMap
          .setText("<html><body>Data/Maps by MapQuest, OpenStreetMap &#169; </body></html>");

      jxHyperlinkOpenStreetMap = new JXHyperlink();
      jxHyperlinkOpenStreetMap.setText("OpenStreetMap Contributors");
      jxHyperlinkOpenStreetMap.setFont(GuiFont.FONT_PLAIN);
      jxHyperlinkOpenStreetMap.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            BrowserUtil.browse(new URI("http://www.openstreetmap.org"));
          }
          catch (URISyntaxException e1) {
          }
        }
      });
      jLabelLicence = new JLabel(" Licence");
      jLabelLicence.setFont(GuiFont.FONT_PLAIN);
      jxHyperlinklicence = new JXHyperlink();
      jxHyperlinklicence.setText("CC-BY-SA");
      jxHyperlinklicence.setFont(GuiFont.FONT_PLAIN);
      jxHyperlinklicence.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            BrowserUtil
                .browse(new URI("http://creativecommons.org/licenses/by-sa/2.0/"));
          }
          catch (URISyntaxException e1) {
          }
        }
      });

      jPanelSouth = new JPanel();
      FlowLayout flowLayout = new FlowLayout();
      flowLayout.setAlignment(FlowLayout.LEFT);
      jPanelSouth.setLayout(flowLayout);
      // jPanelSouth.setLayout(new BoxLayout(jPanelSouth, BoxLayout.Y_AXIS));
      jPanelSouth.setAlignmentY(TOP_ALIGNMENT);
      jPanelSouth.add(jLabelOpenStreeetMap, null);
      jPanelSouth.add(jxHyperlinkOpenStreetMap, null);
      jPanelSouth.add(jLabelLicence, null);
      jPanelSouth.add(jxHyperlinklicence, null);
    }
    return jPanelSouth;
  }

  /**
   * This method initializes jButtonExec
   * 
   * @return javax.swing.JButton
   */
  private JButton getJButtonDelete() {
    if (jButtonDelete == null) {
      jButtonDelete = new JButton();
      jButtonDelete.setFont(GuiFont.FONT_PLAIN);
    }
    return jButtonDelete;
  }

  private String cacheSize() {
    String value;

    long length = AllMapsFactory.getInstance().cacheSize();
    if (length == 0) {
      value = "0 o";
    }
    else if (length < 1024) {
      value = String.valueOf(length) + " o";
    }
    else {
      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setDecimalSeparator('.');
      symbols.setGroupingSeparator(' ');
      DecimalFormat format = new DecimalFormat();
      format.setMaximumFractionDigits(2);
      format.setDecimalFormatSymbols(symbols);
      if (length < 1024 * 1024) {
        value = format.format(length / 1024.0) + " Ko";
      }
      else {
        value = format.format(length / 1024.0 / 1024.0) + " Mo";
      }
    }

    return value;
  }

} // @jve:decl-index=0:visual-constraint="10,10"
