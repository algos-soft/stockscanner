package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.enums.ActionTypes;
import com.algos.stockscanner.data.enums.Actions;
import com.algos.stockscanner.data.enums.Reasons;

public class Decision {
    Actions action;
    ActionTypes actionType;
    Reasons reason;

    public Decision(Actions action, ActionTypes actionType, Reasons reason) {
        this.action=action;
        this.actionType = actionType;
        this.reason = reason;
    }

    public Actions getAction() {
        return action;
    }

    public ActionTypes getActionType() {
        return actionType;
    }

    public Reasons getReason() {
        return reason;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();

        sb.append(action.getCode());

        if(actionType!=null){
            sb.append(" ");
            sb.append(actionType.getCode());
        }

        if(reason!=null){
            sb.append(" ");
            sb.append(reason.getCode());
        }

        return sb.toString();
    }
}
