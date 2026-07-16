import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import AlertNotification from '../AlertNotification.vue';

describe('AlertNotification.vue', () => {
  it('does not render anything if both errorMsg and successMsg are null', () => {
    const wrapper = mount(AlertNotification, {
      props: {
        errorMsg: null,
        successMsg: null,
      },
    });
    expect(wrapper.find('.message-container').exists()).toBe(false);
  });

  it('renders errorMsg and emits closeError when clicked', async () => {
    const wrapper = mount(AlertNotification, {
      props: {
        errorMsg: 'An error occurred',
        successMsg: null,
      },
    });

    // Check error alert is displayed
    expect(wrapper.find('.alert-error').exists()).toBe(true);
    expect(wrapper.find('.alert-error').text()).toContain('An error occurred');
    expect(wrapper.find('.alert-success').exists()).toBe(false);

    // Trigger close action
    const closeBtn = wrapper.find('.alert-error .close-alert');
    expect(closeBtn.exists()).toBe(true);
    await closeBtn.trigger('click');

    // Expect 'closeError' to be emitted
    expect(wrapper.emitted()).toHaveProperty('closeError');
  });

  it('renders successMsg and emits closeSuccess when clicked', async () => {
    const wrapper = mount(AlertNotification, {
      props: {
        errorMsg: null,
        successMsg: 'Operation completed successfully',
      },
    });

    // Check success alert is displayed
    expect(wrapper.find('.alert-success').exists()).toBe(true);
    expect(wrapper.find('.alert-success').text()).toContain('Operation completed successfully');
    expect(wrapper.find('.alert-error').exists()).toBe(false);

    // Trigger close action
    const closeBtn = wrapper.find('.alert-success .close-alert');
    expect(closeBtn.exists()).toBe(true);
    await closeBtn.trigger('click');

    // Expect 'closeSuccess' to be emitted
    expect(wrapper.emitted()).toHaveProperty('closeSuccess');
  });
});
