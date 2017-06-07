describe('Component:  pfABoutModal', function () {
  var $scope;
  var $compile;

  // load the controller's module
  beforeEach(module(
    'patternfly.modals',
    'modals/about-modal.html'
  ));

  beforeEach(inject(function (_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileHtml = function (markup, scope) {
    var element = angular.element(markup);
    $compile(element)(scope);
    scope.$digest();
    return element;
  };

  var closeModal = function(scope) {
    scope.isOpen = false;
    scope.$digest();

    // Although callbacks are executed properly, the modal is not removed in this
    // environment -- must remove it manually to mimic UI Bootstrap.
    var modal = getModal();
    if (modal) {
      modal.remove();
    }
    var modalBackdrop = angular.element(document.querySelector('.modal-backdrop'));
    if (modalBackdrop) {
      modalBackdrop.remove();
    }
  };

  // Modal elements are located in a template, so wait until modal is shown.
  var getModal = function () {
    return angular.element(document.querySelector('.modal'));
  };

  var openModal = function(scope) {
    scope.isOpen = true;
    scope.$digest();
  };

  beforeEach(function () {
    closeModal($scope);
    $scope.copyright = "Copyright Information";
    $scope.imgAlt = "Patternfly Symbol";
    $scope.imgSrc = "img/logo-alt.svg";
    $scope.title = "Product Title";
    $scope.isOpen = true;
    $scope.productInfo = [
      { product: 'Label', version: 'Version' },
      { product: 'Label', version: 'Version' },
      { product: 'Label', version: 'Version' },
      { product: 'Label', version: 'Version' },
      { product: 'Label', version: 'Version' },
      { product: 'Label', version: 'Version' },
      { product: 'Label', version: 'Version' }];
    $scope.open = function () {
      $scope.isOpen = true;
    }
    $scope.onClose = function() {
      $scope.isOpen = false;
    }
  });

  it('should invoke the onClose callback when close button is clicked', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var closeButton = angular.element(getModal()).find('button');
    eventFire(closeButton[0], 'click');
    $scope.$digest();
    expect($scope.isOpen).toBe(false);
  });

  it('should open the about modal via an external button click', function () {
    $scope.isOpen = false;
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var buttonHtml = '<button ng-click="open()" class="btn btn-default">Launch about modal</button>';
    var closeButton = compileHtml(buttonHtml, $scope);
    eventFire(closeButton[0], 'click');
    $scope.$digest();
    expect($scope.isOpen).toBe(true);
    expect(angular.element(getModal()).find('h1').length).toBe(1);
  });

  it('should open the about modal programmatically', function () {
    $scope.isOpen = false;
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    expect(angular.element(getModal()).find('h1').length).toBe(0);
    openModal($scope);
    expect(angular.element(getModal()).find('h1').length).toBe(1);
  });

  it('should not open the about modal', function () {
    var modalHtml = '<pf-about-modal is-open="false" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    expect(angular.element(getModal()).find('h1').length).toBe(0);
    expect(angular.element(getModal()).find('.trademark-pf').length).toBe(0);
  });

  it('should set the product title', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    expect(angular.element(getModal()).find('h1').html()).toBe('Product Title');
  });

  it('should not show product title when a title is not supplied', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    expect(angular.element(getModal()).find('h1').length).toBe(0);
  });

  it('should set the product copyright', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    expect(angular.element(getModal()).find('.trademark-pf').html()).toBe('Copyright Information');
  });

  it('should not show product copyright when a copyright is not supplied', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    expect(angular.element(getModal()).find('.trademark-pf').length).toBe(0);
  });

  it('should set the corner graphic alt text', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var footer = angular.element(getModal()).find('.modal-footer');
    expect(angular.element(footer).find('img').attr('alt')).toBe('Patternfly Symbol');
  });

  it('should not show alt text for corner graphic when imgAlt is not supplied', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var footer = angular.element(getModal()).find('.modal-footer');
    expect(angular.element(footer).find('img').attr('alt').length).toBe(0);
  });

  it('should set the corner graphic src', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var footer = angular.element(getModal()).find('.modal-footer');
    expect(angular.element(footer).find('img').attr('src')).toBe('img/logo-alt.svg');
  });

  it('should not show corner graphic when imgSrc is not supplied', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var footer = angular.element(getModal()).find('.modal-footer');
    expect(angular.element(footer).find('img').length).toBe(0);
  });

  it('should show simple content', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc" product-info="productInfo"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var transclude = angular.element(getModal()).find('.product-versions-pf');
    expect(angular.element(transclude).find('ul').length).toBe(1);
  });

  it('should show custom content', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc"><ul class="list-unstyled"><li><strong>Label</strong> Version</li></ul></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var transclude = angular.element(getModal()).find('.product-versions-pf');
    expect(angular.element(transclude).find('ul').length).toBe(1);
  });

  it('should not show content', function () {
    var modalHtml = '<pf-about-modal is-open="isOpen" on-close="onClose()" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc"></pf-about-modal>';
    compileHtml(modalHtml, $scope);
    var transclude = angular.element(getModal()).find('.product-versions-pf');
    expect(angular.element(transclude).find('ul').length).toBe(0);
  });
});
