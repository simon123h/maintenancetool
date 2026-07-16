import { describe, it, expect } from 'vitest';
import { useForm } from '../useForm';

describe('useForm Composable', () => {
  it('should initialize the form with a copy of initial state', () => {
    const initialState = { name: 'John', age: 30 };
    const { form } = useForm(initialState);

    expect(form.value).toEqual(initialState);
    expect(form.value).not.toBe(initialState); // should be a new object reference
  });

  it('should reset to initial state when reset is called without arguments', () => {
    const initialState = { name: 'John', age: 30 };
    const { form, reset } = useForm(initialState);

    form.value.name = 'Jane';
    form.value.age = 25;

    reset();

    expect(form.value).toEqual(initialState);
  });

  it('should reset to a new custom state when reset is called with arguments', () => {
    const initialState = { name: 'John', age: 30 };
    const { form, reset } = useForm(initialState);

    form.value.name = 'Jane';

    const newState = { name: 'Bob', age: 40 };
    reset(newState);

    expect(form.value).toEqual(newState);
    expect(form.value).not.toBe(newState); // should be a copy
  });
});
