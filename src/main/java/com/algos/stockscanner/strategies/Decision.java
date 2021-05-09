package com.algos.stockscanner.strategies;

import com.algos.stockscanner.enums.ActionTypes;
import com.algos.stockscanner.enums.Actions;
import com.algos.stockscanner.enums.Reasons;

public class Decision {
    Actions action;
    ActionTypes actionType;
    Reasons reason;

    // optional data supporting the decision
    DecisionInfo decisionInfo;

    public Decision(Actions action, ActionTypes actionType, Reasons reason) {
        this.action=action;
        this.actionType = actionType;
        this.reason = reason;
    }

    public Decision(Actions action, ActionTypes actionType) {
        this.action=action;
        this.actionType = actionType;
        this.reason = null;
    }


    public Decision(Actions action) {
        this.action=action;
        this.actionType = null;
        this.reason = null;
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

    public DecisionInfo getDecisionInfo() {
        if(decisionInfo==null){
            decisionInfo=new DecisionInfo();
        }
        return decisionInfo;
    }

    public void setDecisionInfo(DecisionInfo decisionInfo) {
        this.decisionInfo = decisionInfo;
    }

//    @Override
//    public String toString() {
//        StringBuilder sb=new StringBuilder();
//
//        sb.append(action.getCode());
//
//        if(actionType!=null){
//            sb.append(" ");
//            sb.append(actionType.getCode());
//        }
//
//        if(reason!=null){
//            sb.append(" ");
//            sb.append(reason.getCode());
//        }
//
//        return sb.toString();
//    }


    @Override
    public String toString() {
        return "Decision{" +
                "action=" + action +
                ", actionType=" + actionType +
                ", reason=" + reason +
                ", decisionInfo=" + decisionInfo +
                '}';
    }
}
