/*
 * Autopsy Forensic Browser
 *
 * Copyright 2012 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.keywordsearch;

import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.sleuthkit.autopsy.coreutils.StringExtract;
import org.sleuthkit.autopsy.coreutils.StringExtract.StringExtractUnicodeTable.SCRIPT;
import org.sleuthkit.autopsy.ingest.IngestManager;

/**
 *
 * General, not per list, keyword search configuration and status display widget
 */
public class KeywordSearchConfigurationPanel2 extends javax.swing.JPanel {

    private static KeywordSearchConfigurationPanel2 instance = null;
    private final Logger logger = Logger.getLogger(KeywordSearchConfigurationPanel2.class.getName());

    /**
     * Creates new form KeywordSearchConfigurationPanel2
     */
    public KeywordSearchConfigurationPanel2() {
        initComponents();
        customizeComponents();
    }

    public static KeywordSearchConfigurationPanel2 getDefault() {
        if (instance == null) {
            instance = new KeywordSearchConfigurationPanel2();
        }
        return instance;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        activateWidgets();
    }
    
    private void reloadScriptCombo() {
        final KeywordSearchIngestService service = KeywordSearchIngestService.getDefault();
        final SCRIPT currentScript = service.getStringExtractScript();
        int items = scriptCombo.getItemCount();
        for (int i = 0; i<items; ++i) {
            String scriptName = (String) scriptCombo.getItemAt(i);
            if (scriptName.equals(currentScript.toString())) {
                scriptCombo.setSelectedIndex(i);
                break;
            }
        }
        
    }
    
    private void activateWidgets() {
        final KeywordSearchIngestService service = KeywordSearchIngestService.getDefault();        
        skipNSRLCheckBox.setSelected(service.getSkipKnown());
        reloadScriptCombo();
        boolean enable = ! IngestManager.getDefault().isIngestRunning();
        scriptCombo.setEnabled(enable);
        skipNSRLCheckBox.setEnabled(enable);
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        skipNSRLCheckBox = new javax.swing.JCheckBox();
        filesIndexedLabel = new javax.swing.JLabel();
        filesIndexedValue = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        scriptLabel = new javax.swing.JLabel();
        scriptCombo = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();

        skipNSRLCheckBox.setText(org.openide.util.NbBundle.getMessage(KeywordSearchConfigurationPanel2.class, "KeywordSearchConfigurationPanel2.skipNSRLCheckBox.text")); // NOI18N
        skipNSRLCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordSearchConfigurationPanel2.class, "KeywordSearchConfigurationPanel2.skipNSRLCheckBox.toolTipText")); // NOI18N
        skipNSRLCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipNSRLCheckBoxActionPerformed(evt);
            }
        });

        filesIndexedLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchConfigurationPanel2.class, "KeywordSearchConfigurationPanel2.filesIndexedLabel.text")); // NOI18N

        filesIndexedValue.setText(org.openide.util.NbBundle.getMessage(KeywordSearchConfigurationPanel2.class, "KeywordSearchConfigurationPanel2.filesIndexedValue.text")); // NOI18N
        filesIndexedValue.setMaximumSize(null);

        scriptLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchConfigurationPanel2.class, "KeywordSearchConfigurationPanel2.scriptLabel.text")); // NOI18N

        scriptCombo.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordSearchConfigurationPanel2.class, "KeywordSearchConfigurationPanel2.scriptCombo.toolTipText")); // NOI18N
        scriptCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scriptComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filesIndexedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filesIndexedValue, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(skipNSRLCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scriptLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scriptCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addComponent(jSeparator2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scriptLabel)
                    .addComponent(scriptCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(skipNSRLCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filesIndexedValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filesIndexedLabel))
                .addContainerGap(187, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void skipNSRLCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skipNSRLCheckBoxActionPerformed
    KeywordSearchIngestService.getDefault().setSkipKnown(skipNSRLCheckBox.isSelected());
}//GEN-LAST:event_skipNSRLCheckBoxActionPerformed

    private void scriptComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scriptComboActionPerformed
        final String scriptStr = (String)scriptCombo.getSelectedItem(); 
        final SCRIPT script = StringExtract.StringExtractUnicodeTable.scriptForString(scriptStr);
        KeywordSearchIngestService.getDefault().setStringExtractScript(script);
    }//GEN-LAST:event_scriptComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filesIndexedLabel;
    private javax.swing.JLabel filesIndexedValue;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JComboBox scriptCombo;
    private javax.swing.JLabel scriptLabel;
    private javax.swing.JCheckBox skipNSRLCheckBox;
    // End of variables declaration//GEN-END:variables

    private void customizeComponents() {
        this.skipNSRLCheckBox.setSelected(KeywordSearchIngestService.getDefault().getSkipKnown());

        try {
            filesIndexedValue.setText(Integer.toString(KeywordSearch.getServer().queryNumIndexedFiles()));
        } catch (SolrServerException ex) {
            logger.log(Level.WARNING, "Could not get number of indexed files");

        } catch (NoOpenCoreException ex) {
            logger.log(Level.WARNING, "Could not get number of indexed files");
        }

        KeywordSearch.changeSupport.addPropertyChangeListener(KeywordSearch.NUM_FILES_CHANGE_EVT,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        String changed = evt.getPropertyName();
                        Object newValue = evt.getNewValue();

                        if (changed.equals(KeywordSearch.NUM_FILES_CHANGE_EVT)) {
                            int newFilesIndexed = ((Integer) newValue).intValue();
                            filesIndexedValue.setText(Integer.toString(newFilesIndexed));

                        }
                    }
                });

        List<StringExtract.StringExtractUnicodeTable.SCRIPT> supportedScripts = StringExtract.getSupportedScripts();
        for (StringExtract.StringExtractUnicodeTable.SCRIPT s : supportedScripts) {
            scriptCombo.addItem(s.toString());
        }
        reloadScriptCombo();

    }
}
