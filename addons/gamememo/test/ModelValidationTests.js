TestCase("[Gamememo] Model validation tests", {
   setUp: function () {
       this.presenter = Addongamememo_create();

       this.stubs = {
           cssStub: sinon.stub().returns(1)
       };

       this.presenter.viewContainer = {
           css: this.stubs.cssStub
       };

       this.model = {
           "Rows": "1",
           "Columns": "2",
           "Pairs": [{
               'A (text)': "Text A",
               'A (image)': "",
               'B (text)': "Text B",
               'B (image)': ""
           }],
           "Use two styles for cards": "True",
           'Keep cards aspect ratio': "True",
           'Image for style A': "",
           'Image for style B': "",
           'Is Not Activity': "True",
           'Image Mode': "",
           'Keep wrong marking': "True",
           'Time to solve': 10,
           'Session ended message': "",
           'Enable tabindex': "True"
       }
   },

    'test should set isTabindexEnabled to true': function () {
        var upgraded = this.presenter.readConfiguration(this.model);

        assertFalse(upgraded.isError);
        assertTrue(this.presenter.isTabindexEnabled);
    },

    'test should set isTabindexEnabled to false': function () {
        this.model['Enable tabindex'] = "False";

        var upgraded = this.presenter.readConfiguration(this.model);

        assertFalse(upgraded.isError);
        assertFalse(this.presenter.isTabindexEnabled);
    }
});