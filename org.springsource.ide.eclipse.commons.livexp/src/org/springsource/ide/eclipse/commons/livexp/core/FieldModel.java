package org.springsource.ide.eclipse.commons.livexp.core;

public abstract class FieldModel<T> {
	
	private Class<T> type; //Type of data stored in the field.
	private String name; // used to submit value to some service that handles the form
	private String label; // Label to display in forms
	private LiveVariable<T> variable;
	private LiveExpression<ValidationResult> validator;
	
	public FieldModel(Class<T> type, String name, T defaultValue) {
		this.type = type;
		this.name  = name;
		this.label = name;
		this.variable = new LiveVariable<T>(defaultValue);
		this.validator = Validator.OK;
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}

	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}
	
	/**
	 * Specify label (used in forms). By default the name is used.
	 * @return The receiver for easy chaining.
	 */
	public FieldModel<T> label(String l) {
		this.label = l;
		return this;
	}

	public FieldModel<T> validator(LiveExpression<ValidationResult> v) {
		this.validator = v;
		return this;
	}

	public void setValue(T v) {
		this.variable.setValue(v);
	}
	
	public T getValue() {
		return variable.getValue();
	}
	
	public LiveVariable<T> getVariable() {
		return variable;
	}
		

}
