/*
 *  Copyright (C) 2011-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.parser.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author JPEXS
 */
public class SlotConstTraitDetailPanel extends JPanel implements TraitDetail {

    public JEditorPane slotConstEditor;
    private ABC abc;
    private TraitSlotConst trait;
    private boolean showWarning = false;

    public SlotConstTraitDetailPanel() {
        slotConstEditor = new LineMarkedEditorPane();
        setLayout(new BorderLayout());
        //add(new JLabel(translate("abc.detail.slotconst.typevalue")), BorderLayout.NORTH);
        add(new JScrollPane(slotConstEditor), BorderLayout.CENTER);
        /*StyledDocument doc = warnLabel.getStyledDocument();
         SimpleAttributeSet center = new SimpleAttributeSet();
         StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
         doc.setParagraphAttributes(0, doc.getLength(), center, false);
         warnLabel.setOpaque(false);
         warnLabel.setFocusable(false);
         //warnLabel.setWrapStyleWord(true);  
         //warnLabel.setLineWrap(true);
         warnLabel.setFont(new JLabel().getFont().deriveFont(Font.BOLD));
         add(warnLabel, BorderLayout.SOUTH);*/
        slotConstEditor.setContentType("text/flasm3");
        //Flasm3SyntaxKit sk = (Flasm3SyntaxKit) slotConstEditor.getEditorKit();
        //sk.deinstallComponent(slotConstEditor, "jsyntaxpane.components.LineNumbersRuler");
    }

    public void load(TraitSlotConst trait, ABC abc, boolean isStatic) {
        this.abc = abc;
        this.trait = trait;
        /*String s;
         String typeStr;
         if (trait.type_index > 0) {
         typeStr = "m[" + trait.type_index + "]\"" + Helper.escapeString(abc.constants.constant_multiname[trait.type_index].toString(abc.constants, new ArrayList<String>())) + "\"";
         } else {
         typeStr = "*";
         }
         String valueStr = "";
         if (trait.value_kind != 0) {
         valueStr = " = " + (new ValueKind(trait.value_index, trait.value_kind)).toString(abc.constants);
         }

         s = typeStr + valueStr;

         *       */
        String s = "trait " + abc.constants.multinameToString(trait.name_index) + " " + (trait.isConst() ? "const" : "slot") + " slotid " + trait.slot_id + " type " + abc.constants.multinameToString(trait.type_index) + " value " + (new ValueKind(trait.value_index, trait.value_kind).toASMString(abc.constants));

        showWarning = trait.isConst() || isStatic;
        //warnLabel.setVisible(trait.isConst() || isStatic);
        slotConstEditor.setText(s);
    }

    @Override
    public boolean save() {
        try {//(slotConstEditor.getText(), trait, abc)
            if (!ASM3Parser.parseSlotConst(new ByteArrayInputStream(slotConstEditor.getText().getBytes("UTF-8")), abc.constants, trait)) {
                return false;
            }
        } catch (ParseException ex) {
            View.showMessageDialog(slotConstEditor, ex.text, translate("error.slotconst.typevalue"), JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SlotConstTraitDetailPanel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(SlotConstTraitDetailPanel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public void setEditMode(boolean val) {
        if (val && active) {
            JOptionPane.showMessageDialog(null, translate("warning.initializers"), translate("message.warning"), JOptionPane.WARNING_MESSAGE);
        }
        slotConstEditor.setEditable(val);
    }
    private boolean active = false;

    @Override
    public void setActive(boolean val) {
        this.active = val;
    }
}
